import React, { createContext, useContext, useRef, useCallback } from 'react';

interface LogContextType {
  getLogs: (engineId: string) => string[];
  subscribe: (engineId: string, callback: (data: string) => void) => () => void;
  clearLogs: (engineId: string) => void;
}

const LogContext = createContext<LogContextType | null>(null);

const MAX_BUFFER_SIZE = 10000;

export const LogProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const logBuffers = useRef<Record<string, string[]>>({});
  const sockets = useRef<Record<string, WebSocket>>({});
  const listeners = useRef<Record<string, Set<(data: string) => void>>>({});

  const clearLogs = useCallback((engineId: string) => {
    logBuffers.current[engineId] = [];
  }, []);

  const getLogs = useCallback((engineId: string) => {
    return logBuffers.current[engineId] || [];
  }, []);

  const subscribe = useCallback((engineId: string, callback: (data: string) => void) => {
    // 1. Initialize listeners set for this engine
    if (!listeners.current[engineId]) {
      listeners.current[engineId] = new Set();
    }
    listeners.current[engineId].add(callback);

    // 2. Open socket if not exists
    if (!sockets.current[engineId] || sockets.current[engineId].readyState !== WebSocket.OPEN) {
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      const wsUrl = import.meta.env.DEV 
        ? `ws://localhost:8080/api/ws/logs/${engineId}`
        : `${protocol}//${window.location.host}/api/ws/logs/${engineId}`;
      
      const ws = new WebSocket(wsUrl);
      sockets.current[engineId] = ws;

      ws.onmessage = (event) => {
        const data = event.data as string;
        
        // Update buffer
        if (!logBuffers.current[engineId]) {
          logBuffers.current[engineId] = [];
        }
        logBuffers.current[engineId].push(data);
        if (logBuffers.current[engineId].length > MAX_BUFFER_SIZE) {
          logBuffers.current[engineId].shift();
        }

        // Notify all subscribers
        listeners.current[engineId]?.forEach(cb => cb(data));
      };

      ws.onclose = () => {
        console.log(`WebSocket closed for ${engineId}`);
        // Optionally auto-reconnect logic here
      };
    }

    // Return unsubscribe function
    return () => {
      listeners.current[engineId]?.delete(callback);
      // We keep the socket open even if no one is listening (background persistence)
      // unless we want to conserve resources. For now, we keep it for persistence.
    };
  }, []);

  return (
    <LogContext.Provider value={{ getLogs, subscribe, clearLogs }}>
      {children}
    </LogContext.Provider>
  );
};

export const useLogs = () => {
  const context = useContext(LogContext);
  if (!context) {
    throw new Error('useLogs must be used within a LogProvider');
  }
  return context;
};
