import { ReactNode } from 'react';
import Image from "next/image";
import styles from "./page.module.css";


export default function AuthLayout({ children }: { children: ReactNode }) {
    return (
    <div className={styles.parentContainer}>
      <div className={styles.backgroundContainer}>
        <Image
          src="/stars-partial.svg"
          width={500}
          height={500}
          alt="Stars decoration"
          className={styles.stars}
        />

        <Image
          src="/cloud-partial.svg"
          width={800}
          height={800}
          alt="Cloud decoration"
          className={styles.cloud}
        />

        <Image
          src="/planet-partial.svg"
          width={500}
          height={500}
          alt="Planet decoration"
          className={styles.planet}
        />

        <Image
          src="/auth-logo.svg"
          width={500}
          height={88}
          alt="Logo"
          className={styles.auth__logo}
        />
        <p className={styles.slogan}><span className={styles.slogan__color}>Shorten</span>, <span className={styles.slogan__color}>track</span>, and <span className={styles.slogan__color}>optimize</span> your URLs effortlessly powerful tools for businesses, made <span className={styles.slogan__color}>simple</span>.</p>
        <Image
          src="/graph.svg"
          width={400}
          height={400}
          alt="logo"
          className={styles.graph}
        />
      </div>
      <main className={styles.authContent}>
          {children}
        </main>
    </div>
  );
}
