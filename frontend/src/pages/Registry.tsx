import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { registryApi, engineApi } from '../api';
import type { ManagedServer } from '../types';
import { Plus, Trash2, CheckCircle2, Globe, Cpu, RefreshCw, Settings, Terminal } from 'lucide-react';

const Registry: React.FC = () => {
  const navigate = useNavigate();
  const [servers, setServers] = useState<ManagedServer[]>([]);
  const [activeId, setActiveId] = useState<string | null>(null);
  const [showAddForm, setShowAddForm] = useState(false);
  const [newServer, setNewServer] = useState({ name: '', url: '' });
  const [serverStatus, setServerStatus] = useState<Record<string, 'online' | 'offline' | 'checking' | 'unknown'>>({});

  const checkReachability = useCallback(async (server: ManagedServer) => {
    const id = server.id || 'local';
    setServerStatus(prev => ({ ...prev, [id]: 'checking' }));
    try {
      await engineApi.listStubs({ headers: { 'X-Wixy-Target-Server': server.id || 'local' } });
      setServerStatus(prev => ({ ...prev, [id]: 'online' }));
    } catch {
      setServerStatus(prev => ({ ...prev, [id]: 'offline' }));
    }
  }, []);

  const fetchData = useCallback(async () => {
    try {
      const [serversRes, activeRes] = await Promise.all([
        registryApi.listServers(),
        registryApi.getActive(),
      ]);
      setServers(serversRes.data);
      const activeServer = activeRes.data;
      setActiveId(activeServer?.id || 'local');
      
      // Auto-check reachability for all servers
      serversRes.data.forEach((server: ManagedServer) => {
        void checkReachability(server);
      });
    } catch (err: unknown) {
      console.error('Failed to fetch registry data', err);
    }
  }, [checkReachability]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void fetchData();
  }, [fetchData]);

  const handleSetActive = async (id: string | null) => {
    const targetId = id === 'local' ? null : id;
    const currentId = activeId === 'local' ? null : activeId;
    
    if (targetId === currentId) {
      return true;
    }

    try {
      await registryApi.setActive(id);
      setActiveId(id);
      return true;
    } catch (err: unknown) {
      console.error('Failed to set active server', err);
      // Only alert if it's a real failure to switch
      alert('Failed to switch active engine. Make sure the target server is reachable.');
      return false;
    }
  };

  const handleConfigure = async (id: string | null) => {
    const success = await handleSetActive(id);
    if (success) navigate('/dashboard');
  };

  const handleViewLogs = async (id: string | null) => {
    const success = await handleSetActive(id);
    if (success) navigate('/logs');
  };

  const handleAddServer = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await registryApi.addServer(newServer);
      setNewServer({ name: '', url: '' });
      setShowAddForm(false);
      fetchData();
    } catch (err: unknown) {
      console.error('Failed to add server', err);
      alert('Failed to add server');
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure?')) return;
    try {
      await registryApi.removeServer(id);
      fetchData();
    } catch (err: unknown) {
      console.error('Failed to delete server', err);
      alert('Failed to delete server');
    }
  };

  return (
    <div className="space-y-10">
      <div className="flex justify-between items-end">
        <div>
          <h1 className="text-4xl font-black tracking-tight text-white mb-2">Server Registry</h1>
          <p className="text-gray-400 text-lg">Manage and control your WireMock fleet from one place.</p>
        </div>
        <button 
          onClick={() => setShowAddForm(!showAddForm)}
          className="wixy-button-primary flex items-center gap-2"
        >
          <Plus className="w-5 h-5" /> Add Remote Server
        </button>
      </div>

      {showAddForm && (
        <form onSubmit={handleAddServer} className="wixy-card animate-in fade-in slide-in-from-top-4 duration-300">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
            <div className="space-y-2">
              <label className="text-xs font-bold uppercase tracking-widest text-gray-500">Server Name</label>
              <input
                required
                className="w-full bg-black/40 border border-white/10 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:border-wixy-cyan transition-colors"
                placeholder="e.g. Staging API"
                value={newServer.name}
                onChange={(e) => setNewServer({ ...newServer, name: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <label className="text-xs font-bold uppercase tracking-widest text-gray-500">Admin URL</label>
              <input
                required
                className="w-full bg-black/40 border border-white/10 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:border-wixy-cyan transition-colors"
                placeholder="e.g. http://mock-server:8080"
                value={newServer.url}
                onChange={(e) => setNewServer({ ...newServer, url: e.target.value })}
              />
            </div>
          </div>
          <div className="flex gap-3 justify-end">
            <button type="button" onClick={() => setShowAddForm(false)} className="wixy-button-secondary">Cancel</button>
            <button type="submit" className="wixy-button-primary px-8">Save Server</button>
          </div>
        </form>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {servers.map((server) => {
          const id = server.id || 'local';
          const isActive = server.id === activeId || (server.type === 'INTERNAL' && activeId === null);
          const isOffline = serverStatus[id] === 'offline';

          return (
            <div 
              key={id} 
              className={`wixy-card relative overflow-hidden group flex flex-col ${isActive ? 'border-wixy-cyan ring-1 ring-wixy-cyan/50' : ''}`}
            >
              <div className="flex items-start justify-between mb-4">
                <div className={`p-2 rounded-lg ${server.type === 'INTERNAL' ? 'bg-amber-500/10 text-amber-500' : 'bg-wixy-cyan/10 text-wixy-cyan'}`}>
                  {server.type === 'INTERNAL' ? <Cpu className="w-6 h-6" /> : <Globe className="w-6 h-6" />}
                </div>
                {server.type === 'REMOTE' && (
                  <button 
                    onClick={() => handleDelete(server.id!)}
                    className="opacity-0 group-hover:opacity-100 p-2 text-gray-500 hover:text-red-500 transition-all"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                )}
              </div>

              <h3 className="text-xl font-bold text-white mb-1">{server.name}</h3>
              <p className="text-gray-500 font-mono text-xs mb-6 truncate">{server.url}</p>

              <div className="flex items-center justify-between mt-auto pt-4 border-t border-white/5">
                <div className="flex items-center gap-2">
                  {serverStatus[id] === 'checking' ? (
                    <RefreshCw className="w-3 h-3 text-gray-500 animate-spin" />
                  ) : (
                    <div className={`w-2 h-2 rounded-full ${
                      serverStatus[id] === 'online' ? 'bg-green-500 shadow-[0_0_8px_rgba(34,197,94,0.6)]' : 
                      serverStatus[id] === 'offline' ? 'bg-red-500' : 'bg-gray-600'
                    }`} />
                  )}
                  <span className={`text-[10px] font-black uppercase tracking-tighter ${
                    serverStatus[id] === 'online' ? 'text-green-500' : 
                    serverStatus[id] === 'offline' ? 'text-red-500' : 'text-gray-500'
                  }`}>
                    {serverStatus[id] === 'online' ? 'Online' : 
                     serverStatus[id] === 'offline' ? 'Unreachable' : 
                     serverStatus[id] === 'checking' ? 'Checking...' : 'Standby'}
                  </span>
                </div>
                
                <div className="flex gap-4">
                  <button 
                    disabled={isOffline}
                    onClick={() => handleConfigure(server.id)}
                    className={`flex items-center gap-1.5 text-xs font-bold uppercase tracking-widest transition-colors ${
                      isOffline ? 'text-gray-700 cursor-not-allowed' : 'text-wixy-cyan hover:text-cyan-300'
                    }`}
                    title="Configure Engine"
                  >
                    <Settings className="w-3.5 h-3.5" />
                    Config
                  </button>
                  <button 
                    disabled={isOffline}
                    onClick={() => handleViewLogs(server.id)}
                    className={`flex items-center gap-1.5 text-xs font-bold uppercase tracking-widest transition-colors ${
                      isOffline ? 'text-gray-700 cursor-not-allowed' : 'text-wixy-cyan hover:text-cyan-300'
                    }`}
                    title="View Logs"
                  >
                    <Terminal className="w-3.5 h-3.5" />
                    Logs
                  </button>
                </div>
              </div>
              
              {isActive && (
                <div className="absolute top-0 right-0 p-2">
                  <CheckCircle2 className="w-5 h-5 text-wixy-cyan fill-wixy-cyan/10" />
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default Registry;
