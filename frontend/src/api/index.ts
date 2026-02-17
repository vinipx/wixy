import axios from 'axios';
import type { ManagedServer, ProxyStatus, StubMapping, RecordingStatus } from '../types';

const api = axios.create({
  baseURL: '/',
});

// Cache busting interceptor
api.interceptors.request.use((config) => {
  if (config.method === 'get') {
    config.params = {
      ...config.params,
      _t: Date.now(),
    };
  }
  return config;
});

export const registryApi = {
  listServers: () => api.get<ManagedServer[]>('/wixy/admin/registry/servers'),
  addServer: (server: Partial<ManagedServer>) => api.post<ManagedServer>('/wixy/admin/registry/servers', server),
  removeServer: (id: string) => api.delete(`/wixy/admin/registry/servers/${id}`),
  setActive: (id: string | null) => api.post<{ status: string; activeServerId: string }>('/wixy/admin/registry/active', { id: id === 'local' ? null : id }),
  getActive: () => api.get<{ activeServerId: string }>('/wixy/admin/registry/active'),
};

export const engineApi = {
  getProxyStatus: () => api.get<ProxyStatus>('/wixy/admin/proxy'),
  enableProxy: (targetUrl: string) => api.post<{ status: string; targetUrl: string }>('/wixy/admin/proxy/enable', { targetUrl }),
  disableProxy: () => api.post<{ status: string }>('/wixy/admin/proxy/disable'),
  listStubs: () => api.get<{ mappings: StubMapping[]; meta: { total: number } }>('/wixy/admin/mappings'),
  deleteStub: (id: string) => api.delete(`/wixy/admin/mappings/${id}`),
  getRecordingStatus: () => api.get<RecordingStatus>('/wixy/admin/recordings/status'),
  startRecording: (targetUrl?: string) => api.post<{ status: string }>('/wixy/admin/recordings/start', { targetUrl }),
  stopRecording: () => api.post<{ status: string; capturedStubs: number }>('/wixy/admin/recordings/stop'),
};

export default api;
