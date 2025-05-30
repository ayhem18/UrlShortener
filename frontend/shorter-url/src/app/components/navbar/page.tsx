"use client";

import Image from "next/image";
import { usePathname } from 'next/navigation';
import Link from 'next/link';
import styles from "./page.module.css";


export default function Navbar() {
  const pathname = usePathname();

  const navItems = [
    { name: "Home", href: "/components/home" },
    { name: "Features", href: "/components/features" },
    { name: "Pricing", href: "/components/pricing" },
  ];

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
        {navItems.map(({ name, href }) => (
          <li key={name}>
            <Link
              href={href}
              className={`${styles.nav__item} ${pathname  === href ? styles.active : ""}`}
            >
              {name}
            </Link>
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