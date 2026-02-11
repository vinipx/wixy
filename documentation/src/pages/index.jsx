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
          <div className={styles.heroBadge}>Enterprise Service Virtualisation</div>
          <h1 className={styles.heroTitle}>{siteConfig.title}</h1>
          <p className={styles.heroSubtitle}>
            WireMock Proxy Server on Spring Boot
          </p>
          <p className={styles.heroDescription}>
            A lightweight, configurable test proxy service that embeds WireMock
            inside a Spring Boot application — delivering stub management, traffic
            recording, and proxy forwarding with zero infrastructure overhead.
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
    badge: 'STUB',
    color: '#0891b2',
    title: 'Stub Management (CRUD)',
    description:
      'Full REST API for creating, reading, updating, and deleting HTTP stubs at runtime. Pre-packaged stubs loaded from JSON files on startup.',
  },
  {
    badge: 'PROXY',
    color: '#22c55e',
    title: 'Proxy Forwarding',
    description:
      'Transparent proxy mode forwards unmatched requests to a configurable upstream service. Enable or disable at runtime via the Admin API.',
  },
  {
    badge: 'RECORD',
    color: '#8b5cf6',
    title: 'Record & Playback',
    description:
      'Capture real traffic flowing through the proxy, automatically generate stub mappings, and replay them in isolation — perfect for contract testing.',
  },
  {
    badge: 'SECURE',
    color: '#f59e0b',
    title: 'API-Key Security',
    description:
      'Optional API-key header authentication for shared and cloud environments. Health and OpenAPI endpoints remain publicly accessible.',
  },
  {
    badge: 'TEST',
    color: '#ef4444',
    title: '186 Tests · 96.5% Coverage',
    description:
      '129 unit tests + 57 integration tests with JaCoCo enforcement. Integration tests run against local or remote instances — perfect for CI/CD and cloud validation.',
  },
];

const coreFeatures = [
  {
    icon: '🔌',
    title: 'Embedded WireMock',
    description:
      'WireMock runs as a Spring-managed bean with full lifecycle control — automatic startup, graceful shutdown, and health monitoring out of the box.',
  },
  {
    icon: '📋',
    title: 'OpenAPI / Swagger UI',
    description:
      'Auto-generated interactive API documentation at /swagger-ui.html via SpringDoc OpenAPI. Try every endpoint directly from the browser.',
  },
  {
    icon: '⚡',
    title: 'Dual-Port Architecture',
    description:
      'Spring Boot Admin API on port 8080, WireMock stub server on port 9090. Clean separation of management and traffic planes.',
  },
  {
    icon: '📊',
    title: 'Health & Actuator',
    description:
      'Custom WireMock health indicator integrated with Spring Boot Actuator. Real-time stub count, port status, and recording state.',
  },
  {
    icon: '🔧',
    title: 'Profile-Based Config',
    description:
      'Spring profiles (local, docker, cloud) with 12-factor environment variable overrides. Zero code changes between environments.',
  },
  {
    icon: '🚀',
    title: 'Docker & CI/CD Ready',
    description:
      'Multi-stage Dockerfile, Docker Compose, and GitHub Actions workflows. From local development to production-grade deployment in minutes.',
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
