import React, { useState, useEffect } from 'react';
import { X, Save, AlertCircle } from 'lucide-react';
import { engineApi } from '../api';
import type { StubMapping } from '../types';

interface StubEditorProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: () => void;
  initialStub?: StubMapping;
  readOnly?: boolean;
}

const DEFAULT_STUB_JSON = `{
  "request": {
    "method": "GET",
    "url": "/api/resource"
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "message": "Hello World"
    },
    "headers": {
      "Content-Type": "application/json"
    }
  }
}`;

const StubEditor: React.FC<StubEditorProps> = ({ isOpen, onClose, onSave, initialStub, readOnly }) => {
  const [jsonContent, setJsonContent] = useState(DEFAULT_STUB_JSON);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (isOpen) {
      if (initialStub) {
        setJsonContent(JSON.stringify(initialStub, null, 2));
      } else {
        setJsonContent(DEFAULT_STUB_JSON);
      }
      setError(null);
    }
  }, [isOpen, initialStub]);

  const handleSave = async () => {
    if (readOnly) return;
    setSaving(true);
    setError(null);
    try {
      // Validate JSON first
      const parsed = JSON.parse(jsonContent);
      
      // We send the raw JSON string to the backend which expects it
      // The backend AdminController accepts raw JSON string for create/update
      // Wait, we refactored AdminController to accept StubMapping object in previous turns
      // but let's check what the API client does. 
      // The API client sends whatever we pass.
      
      if (initialStub?.id) {
        // Update
        // For update, the ID in the path is what matters, the body is the mapping content
        await engineApi.updateStub(initialStub.id, parsed);
      } else {
        // Create
        await engineApi.createStub(parsed);
      }
      
      onSave();
      onClose();
    } catch (err: unknown) {
      if (err instanceof SyntaxError) {
        setError("Invalid JSON format");
      } else {
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const msg = (err as any).response?.data?.message || (err as Error).message || "Failed to save stub";
        setError(msg);
      }
    } finally {
      setSaving(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-[#161616] border border-[#2a2a2a] w-full max-w-4xl rounded-xl shadow-2xl flex flex-col h-[80vh]">
        
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-white/5">
          <h2 className="text-xl font-bold text-white">
            {readOnly ? 'Stub Details' : (initialStub ? 'Edit Stub Mapping' : 'Create New Stub')}
          </h2>
          <button 
            onClick={onClose}
            className="p-2 text-gray-500 hover:text-white rounded-lg hover:bg-white/5 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Editor Area */}
        <div className="flex-1 p-6 overflow-hidden flex flex-col">
          <div className="mb-4">
            <p className="text-sm text-gray-400 mb-2">
              {readOnly ? 'View the WireMock stub definition below.' : 'Define your WireMock stub using standard JSON format.'}
              {!readOnly && (
                <a href="https://wiremock.org/docs/stubbing/" target="_blank" rel="noreferrer" className="text-wixy-cyan ml-1 hover:underline">
                  Read Documentation
                </a>
              )}
            </p>
          </div>

          <div className="relative flex-1 rounded-lg border border-white/10 overflow-hidden bg-[#0d0d0d]">
            <textarea
              readOnly={readOnly}
              value={jsonContent}
              onChange={(e) => setJsonContent(e.target.value)}
              className="w-full h-full bg-transparent text-gray-300 font-mono text-sm p-4 resize-none focus:outline-none focus:ring-1 focus:ring-wixy-cyan/50 overflow-y-auto"
              spellCheck={false}
            />
          </div>

          {error && (
            <div className="mt-4 p-3 bg-red-500/10 border border-red-500/20 rounded-lg flex items-center gap-3 text-red-400 text-sm">
              <AlertCircle className="w-4 h-4 shrink-0" />
              {error}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-white/5 flex justify-end gap-3 bg-[#1a1a1a]/50">
          <button 
            onClick={onClose}
            className="px-4 py-2 rounded-lg text-sm font-semibold text-gray-400 hover:text-white hover:bg-white/5 transition-colors"
          >
            {readOnly ? 'Close' : 'Cancel'}
          </button>
          {!readOnly && (
            <button
              onClick={handleSave}
              disabled={saving}
              className="flex items-center gap-2 px-6 py-2 bg-wixy-cyan text-white rounded-lg text-sm font-bold hover:bg-cyan-400 transition-colors disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-cyan-900/20"
            >
              {saving ? (
                <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <Save className="w-4 h-4" />
              )}
              {initialStub ? 'Update Stub' : 'Create Stub'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default StubEditor;
