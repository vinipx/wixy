import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import styles from './index.module.css';

function HeroBanner() {
  const { siteConfig } = useDocusaurusContext();
  return (
    <header className={styles.heroBanner}>
      <div className="container">
        <div className={styles.heroContent}>
          <div className={styles.heroBadge}>Enterprise Mock Orchestration</div>
          <h1 className={styles.heroTitle}>WIXY <span style={{ color: '#06b6d4' }}>HUB</span></h1>
          <p className={styles.heroSubtitle}>
            The Central Management Plane for your WireMock Fleet
          </p>
          <p className={styles.heroDescription}>
            A powerful, Spring Boot-based orchestrator that simplifies service virtualisation. 
            Connect local and remote WireMock engines, manage stubs via a modern UI, 
            and automate everything with AI-native MCP tools.
          </p>
          <div className={styles.heroButtons}>
            <Link className={styles.heroPrimary} to="/docs/getting-started/quickstart">
              Get Started →
            </Link>
            <Link className={styles.heroSecondary} to="/docs/architecture/overview">
              View Architecture
            </Link>
          </div>
        </div>
      </div>
    </header>
  );
}

const capabilityFeatures = [
  {
    badge: 'FLEET',
    color: '#f59e0b',
    title: 'Engine Orchestration',
    description:
      'Manage multiple WireMock instances from a single Hub. Register remote servers (Staging, QA, Cloud) and switch context instantly.',
  },
  {
    badge: 'UI',
    color: '#06b6d4',
    title: 'Modern Dashboard',
    description:
      'A sleek React-based UI for managing your fleet. Create, edit, and search stubs visually without writing a single line of code.',
  },
  {
    badge: 'STUB',
    color: '#0891b2',
    title: 'Full Stub CRUD',
    description:
      'Powerful JSON editor with live validation. Manage request matching, response templates, and priority for any registered engine.',
  },
  {
    badge: 'PROXY',
    color: '#22c55e',
    title: 'Per-Engine Proxy',
    description:
      'Configure unique upstream target URLs for each engine. Record live traffic and automatically transform it into replayable stubs.',
  },
  {
    badge: 'AI-NATIVE',
    color: '#8b5cf6',
    title: 'MCP Integration',
    description:
      'Native Model Context Protocol support. Control your proxy, manage stubs, and record traffic using natural language via AI agents.',
  },
  {
    badge: 'SECURE',
    color: '#ef4444',
    title: 'Enterprise Security',
    description:
      'API-key protection for administrative operations. Optimized for multi-tenant and cloud environments with zero infrastructure overhead.',
  },
];

const coreFeatures = [
  {
    icon: '🏢',
    title: 'Fleet Orchestration',
    description:
      'The central Hub acts as a single management plane for multiple WireMock instances across your infrastructure.',
  },
  {
    icon: '🎨',
    title: 'Modern Dashboard',
    description:
      'Manage stubs, recordings, and proxy settings via a high-performance React UI with built-in JSON validation.',
  },
  {
    icon: '🔌',
    title: 'Dual-Mode Engine',
    description:
      'Seamlessly switch between an embedded Local engine and remote instances using the same unified interface.',
  },
  {
    icon: '🤖',
    title: 'AI-Native (MCP)',
    description:
      'Native Model Context Protocol support allows AI agents to configure your test environment using natural language.',
  },
  {
    icon: '🛡️',
    title: 'Multi-Tenant Ready',
    description:
      'Per-request targeting via the X-Wixy-Target-Server header allows sharing a single Hub across multiple projects.',
  },
  {
    icon: '🔧',
    title: 'Zero-Config Setup',
    description:
      'Starts automatically with a pre-configured local engine on port 9090. No external database or setup required.',
  },
  {
    icon: '🚀',
    title: 'Docker & Cloud Optimized',
    description:
      'Lightweight memory footprint and profile-based configuration make it perfect for Kubernetes and CI/CD pipelines.',
  },
];

const techStack = [
  { name: 'Java 21', desc: 'Latest LTS' },
  { name: 'Spring Boot 3.4', desc: 'Framework' },
  { name: 'WireMock 3.13', desc: 'Mock Engine' },
  { name: 'Gradle 9.x', desc: 'Kotlin DSL' },
  { name: 'SpringDoc', desc: 'OpenAPI' },
  { name: 'JaCoCo', desc: 'Coverage' },
  { name: 'RestAssured', desc: 'Test Client' },
  { name: 'Docker', desc: 'Containers' },
];

function CapabilitiesSection() {
  return (
    <section className={styles.capabilities}>
      <div className="container">
        <div className={styles.sectionHeader}>
          <h2>Core Capabilities</h2>
          <p>Everything you need for enterprise HTTP service virtualisation</p>
        </div>
        <div className={styles.capabilityGrid}>
          {capabilityFeatures.map((item, idx) => (
            <div key={idx} className={styles.capabilityCard}>
              <span className={styles.capabilityBadge} style={{ backgroundColor: item.color }}>
                {item.badge}
              </span>
              <h3>{item.title}</h3>
              <p>{item.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function ArchitectureSection() {
  return (
    <section className={styles.architecture}>
      <div className="container">
        <div className={styles.sectionHeader}>
          <h2>Layered Architecture</h2>
          <p>Clean separation of concerns with Spring Boot lifecycle management</p>
        </div>
        <div className={styles.archDiagram}>
          <div className={styles.archLayer} data-layer="4">
            <div className={styles.archLabel}>Layer 4 — REST Controllers</div>
            <div className={styles.archClasses}>
              AdminController · ProxyController · RecordingController
            </div>
          </div>
          <div className={styles.archLayer} data-layer="3">
            <div className={styles.archLabel}>Layer 3 — Services</div>
            <div className={styles.archClasses}>
              StubService · ProxyService · RecordingService
            </div>
          </div>
          <div className={styles.archLayer} data-layer="2">
            <div className={styles.archLabel}>Layer 2 — Configuration</div>
            <div className={styles.archClasses}>
              WixyProperties · SecurityConfig · WireMockConfig
            </div>
          </div>
          <div className={styles.archLayer} data-layer="1">
            <div className={styles.archLabel}>Layer 1 — Embedded WireMock</div>
            <div className={styles.archClasses}>
              WireMockServer · Stub Mappings · Proxy Engine · Recorder
            </div>
          </div>
        </div>
        <div className={styles.archCta}>
          <Link to="/docs/architecture/overview">Explore Full Architecture →</Link>
        </div>
      </div>
    </section>
  );
}

function FeaturesSection() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className={styles.sectionHeader}>
          <h2>Built for Enterprise</h2>
          <p>Production-grade capabilities for mission-critical test environments</p>
        </div>
        <div className={styles.featuresGrid}>
          {coreFeatures.map((item, idx) => (
            <div key={idx} className={styles.featureCard}>
              <div className={styles.featureIcon}>{item.icon}</div>
              <h3>{item.title}</h3>
              <p>{item.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function TechStackSection() {
  return (
    <section className={styles.techStack}>
      <div className="container">
        <div className={styles.sectionHeader}>
          <h2>Technology Stack</h2>
          <p>Modern, battle-tested libraries for enterprise reliability</p>
        </div>
        <div className={styles.techGrid}>
          {techStack.map((item, idx) => (
            <div key={idx} className={styles.techPill}>
              <span className={styles.techName}>{item.name}</span>
              <span className={styles.techDesc}>{item.desc}</span>
            </div>
          ))}
        </div>
        <div className={styles.archCta}>
          <Link to="/docs/getting-started/quickstart">View Full Setup Guide →</Link>
        </div>
      </div>
    </section>
  );
}

function QuickStartSection() {
  return (
    <section className={styles.quickStart}>
      <div className="container">
        <div className={styles.sectionHeader}>
          <h2>Quick Start</h2>
          <p>Up and running in under a minute</p>
        </div>
        <div className={styles.codeBlock}>
          <pre>
            <code>{`# Clone the repository
git clone https://github.com/vinipx/wixy.git
cd wixy

# Start locally (requires Java 21+)
./scripts/start-local.sh

# Or with Docker
./scripts/start-docker.sh

# Create your first stub
curl -X POST http://localhost:8080/wixy/admin/mappings \\
  -H "Content-Type: application/json" \\
  -d '{"request":{"method":"GET","urlPath":"/api/hello"},
       "response":{"status":200,"jsonBody":{"message":"Hello!"}}}'

# Hit it
curl http://localhost:9090/api/hello`}</code>
          </pre>
        </div>
      </div>
    </section>
  );
}

export default function Home() {
  const { siteConfig } = useDocusaurusContext();
  return (
    <Layout
      title="Enterprise WireMock Proxy Server"
      description="WIXY — Lightweight, configurable WireMock proxy server on Spring Boot. Stub management, traffic recording, and proxy forwarding for enterprise test environments."
    >
      <HeroBanner />
      <main>
        <CapabilitiesSection />
        <ArchitectureSection />
        <FeaturesSection />
        <TechStackSection />
        <QuickStartSection />
      </main>
    </Layout>
  );
}
