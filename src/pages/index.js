import React from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import styles from './index.module.css';

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
      <div className="container">
        <h1 className="hero__title">{siteConfig.title}</h1>
        <p className="hero__subtitle">{siteConfig.tagline}</p>
        <div className={styles.buttons}>
          <Link className="button button--secondary button--lg" to="/docs/docs">
            Explore Universal Connector Documentation
          </Link>
        </div>
      </div>
    </header>
  );
}

function HomepageFeatures() {
  const features = [
    {
      title: 'Overview',
      link: '/docs/docs',
      description: 'Start with the main Universal Connectors documentation and repository guide.',
    },
    {
      title: 'Guardium Data Protection',
      link: '/docs/docs/Guardium%20Data%20Protection',
      description: 'Configuration, monitoring, policies, and troubleshooting for GDP deployments.',
    },
    {
      title: 'Guardium Insights',
      link: '/docs/docs/Guardium%20Insights/3.2.x',
      description: 'Guardium Insights configuration, plug-in management, and operational guidance.',
    },
    {
      title: 'Kafka-Based Connectors',
      link: '/docs/docs/KafkaBasedUCs/AlloyDBPubsubKafkaConnect',
      description: 'Connector guides for Kafka, Pub/Sub, Event Hubs, CloudWatch, and JDBC patterns.',
    },
    {
      title: 'Input Plugins',
      link: '/docs/input-plugin/logstash-input-google-pubsub',
      description: 'Input plug-in documentation for Pub/Sub, SQS, S3, JDBC, CloudWatch, and more.',
    },
    {
      title: 'Filter Plugins',
      link: '/docs/filter-plugin/logstash-filter-generic-guardium',
      description: 'Filter plug-in documentation for supported databases and cloud data sources.',
    },
  ];

  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {features.map((feature, idx) => (
            <div key={idx} className={clsx('col col--4', styles.feature)}>
              <Link to={feature.link} className={styles.featureLink}>
                <div className="text--center padding-horiz--md">
                  <h3>{feature.title}</h3>
                  <p>{feature.description}</p>
                </div>
              </Link>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

export default function Home() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title={`${siteConfig.title}`}
      description="Documentation and technical guidance for Guardium Universal Connectors">
      <HomepageHeader />
      <main>
        <HomepageFeatures />
      </main>
    </Layout>
  );
}
