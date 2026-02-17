import React, { useEffect, useState, useCallback } from 'react';
import { engineApi } from '../api';
import type { ProxyStatus, StubMapping } from '../types';
import { 
  Play, Square, Radio, Shield, 
  ArrowRightLeft, FileJson, Activity, 
  Trash2, ExternalLink, RefreshCw 
} from 'lucide-react';

const Dashboard: React.FC = () => {
  const [proxyStatus, setProxyStatus] = useState<ProxyStatus | null>(null);
  const [recordingStatus, setRecordingStatus] = useState<string>('NeverStarted');
  const [stubs, setStubs] = useState<StubMapping[]>([]);
  const [loading, setLoading] = useState(true);
  const [targetUrl, setTargetUrl] = useState('');

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const [proxy, rec, stubsRes] = await Promise.all([
        engineApi.getProxyStatus(),
        engineApi.getRecordingStatus(),
        engineApi.listStubs(),
      ]);
      setProxyStatus(proxy.data);
      setRecordingStatus(rec.data.status);
      setStubs(stubsRes.data.mappings || []);
    } catch (err: unknown) {
      console.error('Failed to fetch dashboard data', err);
      const errorMessage = err instanceof Error ? err.message : String(err);
      alert(`Error fetching data: ${errorMessage}`);
    } finally {
      setLoading(false);
    }
  }, []);

  // Initial load of engine status
  useEffect(() => {
    fetchData();
  }, [fetchData]);

  // Load targetUrl only once when proxyStatus is first available
  useEffect(() => {
    if (proxyStatus?.targetUrl && !targetUrl) {
      setTargetUrl(proxyStatus.targetUrl);
    }
  }, [proxyStatus, targetUrl]);

  const handleToggleProxy = async () => {
    try {
      if (proxyStatus?.enabled) {
        await engineApi.disableProxy();
      } else {
        if (!targetUrl) {
          alert('Please enter a target URL');
          return;
        }
        await engineApi.enableProxy(targetUrl);
      }
      await fetchData();
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : String(err);
      alert(`Failed to toggle proxy: ${errorMessage}`);
    }
  };

  const handleToggleRecording = async () => {
    try {
      if (recordingStatus === 'Recording') {
        await engineApi.stopRecording();
      } else {
        await engineApi.startRecording(targetUrl);
      }
      await fetchData();
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : String(err);
      alert(`Failed to toggle recording: ${errorMessage}`);
    }
  };

  const handleDeleteStub = async (id: string) => {
    if (!confirm('Delete stub?')) return;
    try {
      await engineApi.deleteStub(id);
      fetchData();
    } catch (err: unknown) {
      console.error('Failed to delete stub', err);
      alert('Failed to delete stub');
    }
  };

  if (loading && !proxyStatus) return (
    <div className="flex items-center justify-center min-h-[60vh]">
      <RefreshCw className="w-8 h-8 text-wixy-cyan animate-spin" />
    </div>
  );

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-4xl font-black tracking-tight text-white mb-2">Engine Control</h1>
          <p className="text-gray-400 text-lg flex items-center gap-2">
            Connected to active WireMock instance on port <span className="text-wixy-cyan font-mono font-bold">{proxyStatus?.wiremockPort}</span>
          </p>
        </div>
        <button onClick={fetchData} className="wixy-button-secondary self-start md:self-center flex items-center gap-2">
          <RefreshCw className="w-4 h-4" /> Refresh Status
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Proxy Control Card */}
        <div className="wixy-card flex flex-col">
          <div className="flex items-center gap-3 mb-6">
            <div className={`p-2 rounded-lg ${proxyStatus?.enabled ? 'bg-green-500/10 text-green-500' : 'bg-gray-500/10 text-gray-500'}`}>
              <ArrowRightLeft className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white">Proxy Mode</h3>
              <p className="text-xs text-gray-500 uppercase font-bold tracking-wider">Unmatched Requests</p>
            </div>
          </div>
          
          <div className="space-y-4 flex-1">
            <div className="space-y-2">
              <label className="text-[10px] font-black uppercase tracking-[0.2em] text-gray-500">Target Upstream URL</label>
              <input 
                className="w-full bg-black/40 border border-white/10 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-wixy-cyan"
                placeholder="https://api.example.com"
                value={targetUrl}
                onChange={(e) => setTargetUrl(e.target.value)}
              />
            </div>
          </div>

          <button 
            onClick={handleToggleProxy}
            className={`mt-6 w-full py-3 rounded-lg font-black uppercase tracking-widest transition-all ${
              proxyStatus?.enabled 
                ? 'bg-red-500/10 text-red-500 border border-red-500/20 hover:bg-red-500/20' 
                : 'bg-green-500/10 text-green-500 border border-green-500/20 hover:bg-green-500/20'
            }`}
          >
            {proxyStatus?.enabled ? 'Disable Proxy' : 'Enable Proxy'}
          </button>
        </div>

        {/* Recording Control Card */}
        <div className="wixy-card flex flex-col">
          <div className="flex items-center gap-3 mb-6">
            <div className={`p-2 rounded-lg ${recordingStatus === 'Recording' ? 'bg-red-500/10 text-red-500 animate-pulse' : 'bg-gray-500/10 text-gray-500'}`}>
              <Radio className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white">Traffic Recorder</h3>
              <p className="text-xs text-gray-500 uppercase font-bold tracking-wider">Status: {recordingStatus}</p>
            </div>
          </div>

          <p className="text-sm text-gray-400 flex-1">
            Captures all proxied traffic and automatically creates stub mappings for future replay.
          </p>

          <button 
            onClick={handleToggleRecording}
            className={`mt-6 w-full py-3 rounded-lg font-black uppercase tracking-widest transition-all flex items-center justify-center gap-2 ${
              recordingStatus === 'Recording'
                ? 'bg-white text-black hover:bg-gray-200'
                : 'bg-wixy-cyan text-white hover:bg-cyan-400'
            }`}
          >
            {recordingStatus === 'Recording' ? (
              <><Square className="w-4 h-4 fill-current" /> Stop Recording</>
            ) : (
              <><Play className="w-4 h-4 fill-current" /> Start Recording</>
            )}
          </button>
        </div>

        {/* Quick Info Card */}
        <div className="wixy-card flex flex-col bg-wixy-cyan/5 border-wixy-cyan/20">
          <div className="flex items-center gap-3 mb-6">
            <div className="p-2 rounded-lg bg-wixy-cyan/20 text-wixy-cyan">
              <Activity className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white">Live Stats</h3>
              <p className="text-xs text-gray-500 uppercase font-bold tracking-wider">Real-time Metrics</p>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4 flex-1">
            <div className="bg-black/20 rounded-xl p-4 border border-white/5 text-center">
              <div className="text-3xl font-black text-white">{stubs.length}</div>
              <div className="text-[10px] uppercase font-bold tracking-widest text-gray-500">Active Stubs</div>
            </div>
            <div className="bg-black/20 rounded-xl p-4 border border-white/5 text-center">
              <div className="text-3xl font-black text-white">{proxyStatus?.enabled ? 'ON' : 'OFF'}</div>
              <div className="text-[10px] uppercase font-bold tracking-widest text-gray-500">Proxy</div>
            </div>
          </div>

          <div className="mt-6 p-3 bg-black/40 rounded-lg border border-white/5 flex items-center justify-between">
            <span className="text-[10px] font-bold text-gray-500 uppercase tracking-widest">Health Status</span>
            <span className="flex items-center gap-1.5 text-xs font-bold text-green-500">
              <Shield className="w-3.5 h-3.5" /> SYSTEM UP
            </span>
          </div>
        </div>
      </div>

      {/* Stub Manager Table */}
      <div className="wixy-card !p-0 overflow-hidden">
        <div className="p-6 border-b border-white/5 flex items-center justify-between bg-white/5">
          <div className="flex items-center gap-3">
            <FileJson className="w-5 h-5 text-wixy-cyan" />
            <h3 className="text-xl font-bold text-white">Active Stub Mappings</h3>
          </div>
          <div className="text-xs font-bold text-gray-500 bg-white/5 px-3 py-1.5 rounded-full uppercase tracking-widest border border-white/5">
            {stubs.length} Mappings Registered
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full border-collapse">
            <thead>
              <tr className="bg-black/40">
                <th className="text-left text-[10px] font-black uppercase tracking-[0.2em] text-gray-500 px-6 py-4 border-b border-white/5">Method</th>
                <th className="text-left text-[10px] font-black uppercase tracking-[0.2em] text-gray-500 px-6 py-4 border-b border-white/5">URL Pattern</th>
                <th className="text-left text-[10px] font-black uppercase tracking-[0.2em] text-gray-500 px-6 py-4 border-b border-white/5">Priority</th>
                <th className="text-right text-[10px] font-black uppercase tracking-[0.2em] text-gray-500 px-6 py-4 border-b border-white/5">Actions</th>
              </tr>
            </thead>
            <tbody>
              {stubs.map((stub: StubMapping) => (
                <tr key={stub.id} className="group hover:bg-wixy-cyan/5 transition-colors border-b border-white/5">
                  <td className="px-6 py-4">
                    <span className={`text-[10px] font-black px-2 py-1 rounded border ${
                      stub.request.method === 'GET' ? 'bg-green-500/10 text-green-500 border-green-500/20' :
                      stub.request.method === 'POST' ? 'bg-wixy-cyan/10 text-wixy-cyan border-wixy-cyan/20' :
                      'bg-amber-500/10 text-amber-500 border-amber-500/20'
                    }`}>
                      {stub.request.method || 'ANY'}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm font-mono text-gray-300">
                      {stub.request.url || stub.request.urlPattern || stub.request.urlPath || '/'}
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <span className="text-xs text-gray-500 font-bold">{stub.priority || 5}</span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    <div className="flex justify-end gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                      <button className="p-2 text-gray-500 hover:text-white transition-colors">
                        <ExternalLink className="w-4 h-4" />
                      </button>
                      <button 
                        onClick={() => handleDeleteStub(stub.id)}
                        className="p-2 text-gray-500 hover:text-red-500 transition-colors"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {stubs.length === 0 && (
                <tr>
                  <td colSpan={4} className="px-6 py-12 text-center text-gray-500">
                    No active stub mappings found on this server.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
