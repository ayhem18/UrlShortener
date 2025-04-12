// src/app/components/navbar/Navbar.tsx
"use client";

import Image from "next/image";
import { useState } from 'react';
import Link from 'next/link';
import styles from "./page.module.css";


export default function Navbar() {
  const [activeItem, setActiveItem] = useState('Home');

  return (
    <nav className={styles.nav__container}>
      <Image
        src="/nav-logo.svg"
        width={196}
        height={50}
        alt="logo"
        className={styles.logo}
      />
      <ul className={styles.nav__items}>
        {['Home', 'Features', 'Pricing'].map((item) => (
          <li 
            key={item}
            className={`${styles.nav__item} ${activeItem === item ? styles.active : ''}`}
            onClick={() => setActiveItem(item)}
          >
            {item}
          </li>
        ))}
      </ul>
      <div className={styles.auth}>
        <p className={styles.login}>Login</p>
        <button className={styles.sign}>Sign Up</button>
      </div>
    </nav>
  );
}