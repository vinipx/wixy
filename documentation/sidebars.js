/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  docs: [
    {
      type: "doc",
      id: "index",
      label: "Introduction",
    },
    {
      type: "category",
      label: "Getting Started",
      collapsed: false,
      items: ["getting-started/quickstart"],
    },
    {
      type: "category",
      label: "Architecture",
      collapsed: false,
      items: [
        "architecture/overview",
        "architecture/layers",
      ],
    },
    {
      type: "category",
      label: "Features",
      items: [
        "features/stub-management",
        "features/proxy-mode",
        "features/recording",
        "features/security",
        "features/health-monitoring",
      ],
    },
    {
      type: "category",
      label: "Configuration",
      items: [
        "configuration/profiles",
        "configuration/environment-variables",
      ],
    },
    {
      type: "category",
      label: "API Reference",
      items: ["api/rest-endpoints"],
    },
    {
      type: "category",
      label: "Testing",
      collapsed: false,
      items: [
        "testing/overview",
        "testing/unit-tests",
        "testing/integration-tests",
        "testing/running-locally",
      ],
    },
    {
      type: "category",
      label: "User Guides",
      items: [
        "guides/qa-engineers",
        "guides/developers",
        "guides/devops",
      ],
    },
    {
      type: "category",
      label: "Use Cases & Examples",
      items: ["examples/use-cases"],
    },
    {
      type: "category",
      label: "Deployment",
      items: [
        "deployment/docker",
        "deployment/kubernetes",
        "deployment/ci-cd",
      ],
    },
    {
      type: "category",
      label: "Troubleshooting",
      items: ["troubleshooting/common-issues"],
    },
    {
      type: "category",
      label: "Contributing",
      items: ["contributing/guidelines"],
    },
    {
      type: "doc",
      id: "changelog",
      label: "Changelog",
    },
  ],
};

export default sidebars;
