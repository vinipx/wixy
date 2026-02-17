import axios from 'axios';
import type { AxiosRequestConfig } from 'axios';
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
  listServers: (config?: AxiosRequestConfig) => api.get<ManagedServer[]>('/wixy/admin/registry/servers', config),
  addServer: (server: Partial<ManagedServer>, config?: AxiosRequestConfig) => api.post<ManagedServer>('/wixy/admin/registry/servers', server, config),
  removeServer: (id: string, config?: AxiosRequestConfig) => api.delete(`/wixy/admin/registry/servers/${id}`, config),
  setActive: (id: string | null, config?: AxiosRequestConfig) => api.post<{ status: string; activeServerId: string }>('/wixy/admin/registry/active', { id: id === 'local' ? null : id }, config),
  getActive: (config?: AxiosRequestConfig) => api.get<{ activeServerId: string }>('/wixy/admin/registry/active', config),
};

export const engineApi = {
  getProxyStatus: (config?: AxiosRequestConfig) => api.get<ProxyStatus>('/wixy/admin/proxy', config),
  enableProxy: (targetUrl: string, config?: AxiosRequestConfig) => api.post<{ status: string; targetUrl: string }>('/wixy/admin/proxy/enable', { targetUrl }, config),
  disableProxy: (config?: AxiosRequestConfig) => api.post<{ status: string }>('/wixy/admin/proxy/disable', config),
  listStubs: (config?: AxiosRequestConfig) => api.get<{ mappings: StubMapping[]; meta: { total: number } }>('/wixy/admin/mappings', config),
  createStub: (stub: unknown, config?: AxiosRequestConfig) => api.post<StubMapping>('/wixy/admin/mappings', stub, config),
  updateStub: (id: string, stub: unknown, config?: AxiosRequestConfig) => api.put<StubMapping>(`/wixy/admin/mappings/${id}`, stub, config),
  deleteStub: (id: string, config?: AxiosRequestConfig) => api.delete(`/wixy/admin/mappings/${id}`, config),
  getRecordingStatus: (config?: AxiosRequestConfig) => api.get<RecordingStatus>('/wixy/admin/recordings/status', config),
  startRecording: (targetUrl?: string, config?: AxiosRequestConfig) => api.post<{ status: string }>('/wixy/admin/recordings/start', { targetUrl }, config),
  stopRecording: (config?: AxiosRequestConfig) => api.post<{ status: string; capturedStubs: number }>('/wixy/admin/recordings/stop', config),
};

export default api;
