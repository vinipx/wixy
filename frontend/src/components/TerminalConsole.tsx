import React, { useEffect, useRef, useState, useCallback } from 'react';
import { Terminal } from '@xterm/xterm';
import { FitAddon } from '@xterm/addon-fit';
import { SearchAddon } from '@xterm/addon-search';
import '@xterm/xterm/css/xterm.css';
import { Play, Pause, Download, Search, RefreshCw } from 'lucide-react';
import { useLogs } from '../context/LogContext';

interface TerminalConsoleProps {
  engineId: string;
}

const TerminalConsole: React.FC<TerminalConsoleProps> = ({ engineId }) => {
  const { getLogs, subscribe, clearLogs } = useLogs();
  const terminalRef = useRef<HTMLDivElement>(null);
  const terminalInstance = useRef<Terminal | null>(null);
  const fitAddon = useRef<FitAddon | null>(null);
  const searchAddon = useRef<SearchAddon | null>(null);
  const [isPaused, setIsPaused] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [isConnected] = useState(true); // Context manages connection
  const logBuffer = useRef<string[]>([]); // For paused state
  
  const writeToTerminal = useCallback((data: string) => {
    // Split by literal \r\n text or actual newline characters
    const lines = data.split(/\\r\\n|\\n|\r\n|\n/);
    
    lines.forEach(line => {
      if (line.trim()) {
        if (isPaused) {
          logBuffer.current.push(line);
        } else {
          terminalInstance.current?.writeln(line);
        }
      }
    });
  }, [isPaused]);

  useEffect(() => {
    if (!terminalRef.current) return;

    // Initialize Terminal
    const term = new Terminal({
      theme: {
        background: '#0d1117',
        foreground: '#e6edf3',
        cursor: '#2f81f7',
        selectionBackground: '#388bfd33',
        black: '#484f58',
        red: '#ff7b72',
        green: '#3fb950',
        yellow: '#d29922',
        blue: '#58a6ff',
        magenta: '#bc8cff',
        cyan: '#39c5cf',
        white: '#b1bac4',
      },
      fontFamily: "'Fira Code', 'JetBrains Mono', monospace",
      fontSize: 14,
      cursorBlink: true,
      convertEol: true,
      scrollback: 10000,
    });

    const fit = new FitAddon();
    const search = new SearchAddon();
    
    term.loadAddon(fit);
    term.loadAddon(search);
    
    term.open(terminalRef.current);
    fit.fit();

    terminalInstance.current = term;
    fitAddon.current = fit;
    searchAddon.current = search;

    // 1. Load existing logs from context
    const existingLogs = getLogs(engineId);
    existingLogs.forEach(log => {
      const lines = log.split(/\\r\\n|\\n|\r\n|\n/);
      lines.forEach(line => {
        if (line.trim()) term.writeln(line);
      });
    });

    // 2. Subscribe to new logs
    const unsubscribe = subscribe(engineId, (data) => {
      writeToTerminal(data);
    });

    // Handle Resize
    const handleResize = () => {
      fit.fit();
    };
    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      unsubscribe();
      term.dispose();
    };
  }, [engineId, subscribe, getLogs, writeToTerminal]);

  useEffect(() => {
    // Flush buffer when unpaused
    if (!isPaused && logBuffer.current.length > 0 && terminalInstance.current) {
      logBuffer.current.forEach(log => terminalInstance.current?.writeln(log));
      logBuffer.current = [];
    }
  }, [isPaused]);

  const handleSearch = () => {
    if (searchQuery && searchAddon.current) {
      searchAddon.current.findNext(searchQuery);
    }
  };

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchQuery(e.target.value);
    if (e.target.value === '' && terminalInstance.current) {
       searchAddon.current?.clearDecorations();
    }
  };

  const handleSearchKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  const handleDownloadLogs = () => {
    const allLogs = getLogs(engineId).join('\n');
    const blob = new Blob([allLogs], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `engine-logs-${engineId}.txt`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const handleClearTerminal = () => {
    terminalInstance.current?.clear();
    clearLogs(engineId);
    logBuffer.current = [];
  };

  return (
    <div className="flex flex-col h-full bg-[#0d1117] rounded-xl overflow-hidden border border-white/10 shadow-2xl">
      {/* Toolbar */}
      <div className="flex items-center justify-between p-3 border-b border-white/10 bg-[#161b22]">
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2">
            <div className={`w-2.5 h-2.5 rounded-full ${isConnected ? 'bg-green-500' : 'bg-red-500'} animate-pulse`}></div>
            <span className="text-sm font-semibold text-gray-300 font-mono">
              {engineId} {isConnected ? 'Connected' : 'Disconnected'}
            </span>
          </div>
          <div className="h-4 w-px bg-white/10 mx-2"></div>
          <button
            onClick={() => setIsPaused(!isPaused)}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-bold uppercase tracking-wider transition-colors ${
              isPaused 
                ? 'bg-yellow-500/20 text-yellow-400 hover:bg-yellow-500/30' 
                : 'bg-white/5 text-gray-300 hover:bg-white/10'
            }`}
          >
            {isPaused ? <Play className="w-3.5 h-3.5" /> : <Pause className="w-3.5 h-3.5" />}
            {isPaused ? 'Resume' : 'Pause'}
          </button>
          
          <button
            onClick={handleClearTerminal}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-bold uppercase tracking-wider bg-white/5 text-gray-300 hover:bg-white/10 transition-colors"
          >
            Clear
          </button>
          
          <button
            onClick={() => subscribe(engineId, writeToTerminal)}
            title="Reconnect"
            className="p-1.5 rounded-md text-gray-400 hover:text-white hover:bg-white/10 transition-colors"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>

        <div className="flex items-center gap-3">
          <div className="relative flex items-center">
            <Search className="w-4 h-4 text-gray-400 absolute left-2" />
            <input
              type="text"
              placeholder="Search logs..."
              value={searchQuery}
              onChange={handleSearchChange}
              onKeyDown={handleSearchKeyPress}
              className="bg-[#0d1117] border border-white/10 rounded-md py-1 pl-8 pr-3 text-sm text-white focus:outline-none focus:border-wixy-cyan w-48 transition-colors"
            />
          </div>
          
          <button
            onClick={handleDownloadLogs}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-bold uppercase tracking-wider bg-wixy-cyan/10 text-wixy-cyan hover:bg-wixy-cyan/20 transition-colors"
            title="Download Logs"
          >
            <Download className="w-3.5 h-3.5" />
            Export
          </button>
        </div>
      </div>

      {/* Terminal Container */}
      <div className="flex-1 w-full relative">
        <div ref={terminalRef} className="absolute inset-0 p-2" />
      </div>
    </div>
  );
};

export default TerminalConsole;
