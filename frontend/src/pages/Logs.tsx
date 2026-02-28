import React, { useState, useEffect } from 'react';
import TerminalConsole from '../components/TerminalConsole';
import { Terminal } from 'lucide-react';
import { registryApi } from '../api';
import type { ManagedServer } from '../types';

const Logs: React.FC = () => {
  const [engineId, setEngineId] = useState('local');
  const [activeServer, setActiveServer] = useState<ManagedServer | null>(null);

  useEffect(() => {
    // Attempt to sync with the currently active server in the Hub
    const syncActiveEngine = async () => {
      try {
        const response = await registryApi.getActive();
        if (response.data) {
          setEngineId(response.data.id || 'local');
          setActiveServer(response.data);
        }
      } catch (error) {
        console.error('Failed to fetch active engine', error);
      }
    };
    syncActiveEngine();
  }, []);

  return (
    <div className="flex flex-col h-[calc(100vh-140px)] gap-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-black tracking-tight text-white flex items-center gap-3">
            <Terminal className="w-8 h-8 text-wixy-cyan" />
            Live Logs
          </h1>
          <p className="text-gray-400 mt-2 text-sm max-w-2xl">
            Stream real-time HTTP requests, responses, and engine lifecycle events.
          </p>
        </div>
        
        <div className="flex items-center gap-3 bg-white/5 p-2 rounded-lg border border-white/10">
          <label className="text-xs font-bold text-gray-400 uppercase tracking-wider ml-2">Watching:</label>
          <div className="flex flex-col">
            <span className="text-[10px] font-black text-wixy-cyan uppercase tracking-wider leading-none mb-1">
              {activeServer?.name || 'Local Server'}
            </span>
            <div className="bg-[#0d0d0d] border border-white/10 rounded-md px-3 py-1 text-[10px] text-gray-400 font-mono min-w-32 truncate max-w-48">
              {engineId}
            </div>
          </div>
          <button 
            onClick={() => {
              setEngineId('local');
              setActiveServer(null);
            }}
            className="text-[10px] uppercase font-bold text-gray-500 hover:text-white transition-colors px-2"
          >
            Reset
          </button>
        </div>
      </div>

      <div className="flex-1 min-h-0">
        <TerminalConsole engineId={engineId} />
      </div>
    </div>
  );
};

export default Logs;
