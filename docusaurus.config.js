const {themes: prismThemes} = require('prism-react-renderer');

const baseUrl = process.env.DOCUSAURUS_BASE_URL || '/universal-connectors/';

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Universal Connectors',
  tagline: 'Documentation and technical guidance for Guardium Universal Connectors',
  favicon: 'docs/images/icon_guc.jpg',

  future: {
    v4: true,
  },

  url: process.env.DOCUSAURUS_URL || 'https://ibm.github.io',
  baseUrl,
  organizationName: 'IBM',
  projectName: 'universal-connectors',
  deploymentBranch: 'gh-pages',
  trailingSlash: false,

  onBrokenLinks: 'warn',
  markdown: {
    format: 'md',
    hooks: {
      onBrokenMarkdownLinks: 'warn',
      onBrokenMarkdownImages: 'ignore',
    },
  },

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  staticDirectories: ['static', 'site-content/static'],

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          path: 'site-content/docs',
          routeBasePath: 'docs',
          sidebarPath: require.resolve('./sidebars.js'),
          editUrl: 'https://github.com/IBM/universal-connectors/edit/main/',
          breadcrumbs: true,
          showLastUpdateAuthor: false,
          showLastUpdateTime: false,
          sidebarCollapsible: true,
          sidebarCollapsed: true,
          include: ['**/*.md'],
        },
        blog: false,
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      }),
    ],
  ],

  themes: [
    [
      require.resolve('@easyops-cn/docusaurus-search-local'),
      {
        hashed: true,
        docsRouteBasePath: '/docs',
        indexBlog: false,
      },
    ],
    '@docusaurus/theme-mermaid',
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      image: 'docs/images/icon_guc.jpg',
      docs: {
        sidebar: {
          hideable: true,
          autoCollapseCategories: true,
        },
      },
      colorMode: {
        respectPrefersColorScheme: true,
      },
      navbar: {
        title: 'Universal Connectors',
        logo: {
          alt: 'Universal Connectors',
          src: 'docs/images/icon_guc.jpg',
        },
        items: [
          {
            to: '/docs/docs',
            position: 'left',
            label: 'Overview',
          },
          {
            to: '/docs/docs/Guardium%20Data%20Protection',
            label: 'GDP',
            position: 'left',
          },
          {
            to: '/docs/docs/Guardium%20Insights/3.2.x',
            label: 'GI',
            position: 'left',
          },
          {
            to: '/docs/docs/KafkaBasedUCs/AlloyDBPubsubKafkaConnect',
            label: 'Kafka UCs',
            position: 'left',
          },
          {
            to: '/docs/input-plugin/logstash-input-google-pubsub',
            label: 'Input Plugins',
            position: 'left',
          },
          {
            to: '/docs/filter-plugin/logstash-filter-generic-guardium',
            label: 'Filter Plugins',
            position: 'left',
          },
          {
            href: 'https://github.com/IBM/universal-connectors',
            label: 'GitHub',
            position: 'right',
          },
          {
            type: 'search',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        copyright: `Copyright © ${new Date().getFullYear()} IBM Corp. Built with Docusaurus.`,
      },
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
      },
      tableOfContents: {
        minHeadingLevel: 2,
        maxHeadingLevel: 4,
      },
    }),
};

module.exports = config;
