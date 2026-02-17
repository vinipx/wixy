// @ts-check

import { themes as prismThemes } from "prism-react-renderer";

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: "WIXY",
  tagline: "Enterprise WireMock Proxy Server",
  favicon: "img/logo.svg",

  url: "https://vinipx.github.io",
  baseUrl: "/docs/",

  organizationName: "vinipx",
  projectName: "wixy",

  onBrokenLinks: "ignore",

  i18n: {
    defaultLocale: "en",
    locales: ["en"],
  },

  markdown: {
    mermaid: true,
    hooks: {
      onBrokenMarkdownLinks: "warn",
    },
  },

  themes: ["@docusaurus/theme-mermaid"],

  presets: [
    [
      "classic",
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: "./sidebars.js",
          editUrl: "https://github.com/vinipx/wixy/tree/main/documentation/",
          showLastUpdateTime: true,
        },
        blog: false,
        theme: {
          customCss: "./src/css/custom.css",
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      image: "img/wixy-social-card.png",

      mermaid: {
        theme: {
          light: "base",
          dark: "dark",
        },
        options: {
          themeVariables: {
            primaryColor: "#1a1a2e",
            primaryTextColor: "#e2e8f0",
            primaryBorderColor: "#06b6d4",
            lineColor: "#22d3ee",
            secondaryColor: "#1e1e2e",
            tertiaryColor: "#ecfeff",
          },
        },
      },

      announcementBar: {
        id: "wixy_v1",
        content:
          '⚡ WIXY — Enterprise WireMock Proxy Server on Spring Boot. <a target="_blank" rel="noopener noreferrer" href="https://github.com/vinipx/wixy">Star us on GitHub</a>',
        backgroundColor: "#111111",
        textColor: "#d4d4d8",
        isCloseable: true,
      },

      colorMode: {
        defaultMode: "dark",
        disableSwitch: false,
        respectPrefersColorScheme: true,
      },

      navbar: {
        title: "WIXY",
        logo: {
          alt: "WIXY Logo",
          src: "img/logo.svg",
          srcDark: "img/logo-dark.svg",
          width: 36,
          height: 36,
        },
        items: [
          {
            type: "docSidebar",
            sidebarId: "docs",
            position: "left",
            label: "Documentation",
          },
          {
            to: "/docs/getting-started/quickstart",
            label: "Getting Started",
            position: "left",
          },
          {
            to: "/docs/architecture/overview",
            label: "Architecture",
            position: "left",
          },
          {
            to: "/docs/mcp/overview",
            label: "AI-Native (MCP)",
            position: "left",
          },
          {
            to: "/docs/api/rest-endpoints",
            label: "API Reference",
            position: "left",
          },
          {
            to: "/docs/testing/overview",
            label: "Testing",
            position: "left",
          },
          {
            type: "dropdown",
            label: "Guides",
            position: "left",
            items: [
              {
                to: "/docs/guides/qa-engineers",
                label: "QA Engineers",
              },
              {
                to: "/docs/guides/developers",
                label: "Developers",
              },
              {
                to: "/docs/guides/devops",
                label: "DevOps",
              },
            ],
          },
          {
            type: "dropdown",
            label: "Resources",
            position: "left",
            items: [
              {
                to: "/docs/examples/use-cases",
                label: "Use Cases & Examples",
              },
              {
                to: "/docs/deployment/docker",
                label: "Deployment",
              },
              {
                to: "/docs/troubleshooting/common-issues",
                label: "Troubleshooting",
              },
              {
                to: "/docs/contributing/guidelines",
                label: "Contributing",
              },
              {
                to: "/docs/changelog",
                label: "Changelog",
              },
            ],
          },
          {
            href: "https://github.com/vinipx/wixy",
            label: "GitHub",
            position: "right",
          },
        ],
      },

      footer: {
        style: "dark",
        links: [
          {
            title: "Documentation",
            items: [
              { label: "Getting Started", to: "/docs/getting-started/quickstart" },
              { label: "Architecture", to: "/docs/architecture/overview" },
              { label: "AI-Native (MCP)", to: "/docs/mcp/overview" },
              { label: "API Reference", to: "/docs/api/rest-endpoints" },
              { label: "Testing", to: "/docs/testing/overview" },
            ],
          },
          {
            title: "Guides",
            items: [
              { label: "QA Engineers", to: "/docs/guides/qa-engineers" },
              { label: "Developers", to: "/docs/guides/developers" },
              { label: "DevOps", to: "/docs/guides/devops" },
            ],
          },
          {
            title: "Resources",
            items: [
              { label: "Use Cases", to: "/docs/examples/use-cases" },
              { label: "Deployment", to: "/docs/deployment/docker" },
              { label: "Troubleshooting", to: "/docs/troubleshooting/common-issues" },
            ],
          },
          {
            title: "Links",
            items: [
              { label: "GitHub", href: "https://github.com/vinipx/wixy" },
              { label: "Contributing", to: "/docs/contributing/guidelines" },
              { label: "Changelog", to: "/docs/changelog" },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} WIXY — MIT License`,
      },

      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
        additionalLanguages: [
          "java",
          "kotlin",
          "groovy",
          "bash",
          "json",
          "properties",
          "yaml",
          "markup",
          "docker",
        ],
      },

      tableOfContents: {
        minHeadingLevel: 2,
        maxHeadingLevel: 4,
      },
    }),
};

export default config;
