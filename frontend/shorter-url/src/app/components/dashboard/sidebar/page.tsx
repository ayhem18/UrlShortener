"use client";

import Image from "next/image";
import { useState } from "react";
import { usePathname } from 'next/navigation';
import Link from 'next/link';
import styles from "./page.module.css";
import HomeIcon from '@/assets/home-icon.svg';
import ProfileIcon from '@/assets/users-icon.svg';
import NotifIcon from '@/assets/notif-icon.svg';
import SettingsIcon from '@/assets/settings-icon.svg';


export default function SideBar() {
  const [ripple, setRipple] = useState<string | null>(null);
  const [active, setActive] = useState<string | null>("home");

  const handleClick = (name: string) => {
    setRipple(name);
    setActive(name);
    setTimeout(() => setRipple(null), 400);
  };

  const iconClass = (name: string) => {
    const classes = [styles["icon__paint"]];
    if (ripple === name) classes.push(styles.ripple);
    if (active === name) classes.push(styles.selected);
    return classes.join(" ");
  };
  return (
    <nav className={styles.sidebar__container}>
      <div className={styles.sidebar__icons}>
        <Link href="/components/dashboard/home">
          <div className={iconClass("home")} onClick={() => handleClick("home")}>
            <HomeIcon className={styles.sidebar__icon} />
          </div>
        </Link>
        <Link href="/components/dashboard/admin">

          <div className={iconClass("admin")} onClick={() => handleClick("admin")}>
            <ProfileIcon className={styles.sidebar__icon} />
          </div>
        </Link>
        <Link href="/components/dashboard/profile">

          <div className={iconClass("profile")} onClick={() => handleClick("profile")}>
            <NotifIcon className={styles.sidebar__icon} /></div>
        </Link>
        <Link href="/components/dashboard/home">

          <div className={iconClass("settings")} onClick={() => handleClick("settings")}>
            <SettingsIcon className={styles.sidebar__icon} />
          </div>
        </Link>
      </div>
    </nav>
  );
}