import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Server, Activity, Github, Terminal } from 'lucide-react';

const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const location = useLocation();

  const navItems = [
    { name: 'Registry', path: '/', icon: Server },
    { name: 'Dashboard', path: '/dashboard', icon: Activity },
    { name: 'Logs', path: '/logs', icon: Terminal },
  ];

  return (
    <div className="min-h-screen flex flex-col">
      <nav className="sticky top-0 z-50 bg-[#0d0d0d]/80 backdrop-blur-md border-b border-white/10 px-6 py-3 flex items-center justify-between">
        <div className="flex items-center gap-8">
          <Link to="/" className="flex items-center gap-3 no-underline">
            <div className="p-1 rounded-lg">
              <img src="/vite.svg" alt="WIXY Logo" className="w-8 h-8" />
            </div>
            <span className="font-black text-xl tracking-tighter text-white">WIXY <span className="text-wixy-cyan">HUB</span></span>
          </Link>
          
          <div className="hidden md:flex items-center gap-1">
            {navItems.map((item) => (
              <Link
                key={item.path}
                to={item.path}
                className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-bold uppercase tracking-wider transition-all no-underline ${
                  location.pathname === item.path
                    ? 'text-wixy-cyan bg-wixy-cyan/10'
                    : 'text-gray-400 hover:text-white hover:bg-white/5'
                }`}
              >
                <item.icon className="w-4 h-4" />
                {item.name}
              </Link>
            ))}
          </div>
        </div>

        <div className="flex items-center gap-4">
          <a href="https://github.com/vinipx/wixy" target="_blank" rel="noreferrer" className="text-gray-400 hover:text-white transition-colors">
            <Github className="w-5 h-5" />
          </a>
        </div>
      </nav>

      <main className="flex-1 container mx-auto px-6 py-10 max-w-7xl">
        {children}
      </main>

      <footer className="bg-[#080808] border-t border-white/5 px-6 py-8">
        <div className="container mx-auto max-w-7xl flex flex-col md:flex-row justify-between items-center gap-4">
          <p className="text-gray-500 text-sm font-medium">
            Copyright © {new Date().getFullYear()} WIXY — MIT License
          </p>
          <div className="flex gap-6 text-gray-500 text-xs font-bold uppercase tracking-widest">
            <a href="/docs/" target="_blank" rel="noreferrer" className="hover:text-wixy-cyan transition-colors no-underline">Documentation</a>
            <a href="#" className="hover:text-wixy-cyan transition-colors no-underline">Support</a>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Layout;
