import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Registry from './pages/Registry';
import Dashboard from './pages/Dashboard';
import Logs from './pages/Logs';
import { LogProvider } from './context/LogContext';

function App() {
  return (
    <LogProvider>
      <Router>
        <Layout>
          <Routes>
            <Route path="/" element={<Registry />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/logs" element={<Logs />} />
          </Routes>
        </Layout>
      </Router>
    </LogProvider>
  );
}

export default App;
