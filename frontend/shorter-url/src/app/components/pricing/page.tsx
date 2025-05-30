"use client";

import { redirect } from 'next/navigation';
import styles from "./page.module.css";
import Image from "next/image";
import { useState } from "react";

export default function Home() {

  return (
    <div className={styles.bg_style}>
      <Image
        src="/stars.svg"
        width={350}
        height={350}
        alt="Stars decoration"
        className={styles.stars}
      />
      <Image
        src="/planet.svg"
        width={300}
        height={300}
        alt="Planet decoration"
        className={styles.planet}
      />
      <div className={styles.pricing_components}>
        <div className={styles.description}>
          <p className={styles.desc_title}>Pricing for brands and businesses of all sizes</p>
          <div className={styles.desc}>
            <p>Use our URL shortener to engage your audience and connect them through <span className={styles.desc_url}>efficient</span> links.</p>
          </div>
        </div>
        <div className={styles.offers}>
          <div className={styles.card}>
            <div className={styles.card_header}>
              <p className={styles.card_title}>Free</p>
            </div>
            <div className={styles.card_content}>
              <p className={styles.pricing}><span className={styles.price}>0₽</span>/month</p>
              <p className={styles.trial}>Try it out for free</p>
              <div className={styles.benifits}>
                <p><span style={{ fontWeight: 700 }}>2</span> Employees | <span style={{ fontWeight: 700 }}>1</span> Admin | <span style={{ fontWeight: 700 }}>1</span> Owner</p>
                <p><span style={{ fontWeight: 700 }}>10</span> URLs per Day for each user</p>
                <p>Slower URL processing</p>
                <p>No access to history</p>
              </div>
              <button className={styles.chooseBtn}>Choose Plan</button>
            </div>
          </div>
          <div className={styles.card}>
            <div className={styles.card_header}>
              <p className={styles.card_title}>Tier One</p>
            </div>
            <div className={styles.card_content}>
              <p className={styles.pricing}><span className={styles.price}>99₽</span>/month</p>
              <p className={styles.trial}>Try all core features</p>
              <div className={styles.benifits}>
                <p><span style={{ fontWeight: 700 }}>6</span> Employees | <span style={{ fontWeight: 700 }}>2</span> Admin | <span style={{ fontWeight: 700 }}>1</span> Owner</p>
                <p><span style={{ fontWeight: 700 }}>30</span> URLs per Day for each user</p>
                <p>Faster URL processing</p>
                <p>Access to history</p>
              </div>
              <button className={styles.chooseBtn}>Choose Plan</button>
            </div>
          </div>
          <div className={styles.card}>
            <div className={styles.card_header}>
              <p className={styles.card_infinity}>Infinity</p>
            </div>
            <div className={styles.card_content}>
              <p className={styles.pricing}><span className={styles.price}>199₽</span>/month</p>
              <p className={styles.trial}>Try <span className={styles.infinity_style}>Infinity</span> & Beyond</p>
              <div className={styles.benifits}>
                <p style={{fontSize: '17px' }}><span style={{ fontWeight: 700}}>10</span> Employees | <span style={{ fontWeight: 700 }}>3</span> Admin | <span style={{ fontWeight: 700 }}>1</span> Owner</p>
                <p><span style={{ fontWeight: 700 }} className={styles.infinity_style}>Infinite</span> URLs for each user</p>
                <p>Faster URL processing</p>
                <p>Access to history</p>
              </div>
              <button className={styles.chooseBtn_infinity}>Choose Plan</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}