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
  const pathname = usePathname();

  const routes = [
    { name: "home", path: "/components/dashboard/home", icon: <HomeIcon className={styles.sidebar__icon} /> },
    { name: "admin", path: "/components/dashboard/admin", icon: <ProfileIcon className={styles.sidebar__icon} /> },
    { name: "profile", path: "/components/dashboard/profile", icon: <NotifIcon className={styles.sidebar__icon} /> },
    { name: "settings", path: "/components/dashboard/settings", icon: <SettingsIcon className={styles.sidebar__icon} /> },
  ];
  const handleClick = (name: string) => {
    setRipple(name);
    setActive(name);
    setTimeout(() => setRipple(null), 400);
  };

  const iconClass = (path: string, name: string) => {
    const classes = [styles.icon__paint];
    if (ripple === name) classes.push(styles.ripple);
    if (pathname === path) classes.push(styles.selected);
    return classes.join(" ");
  };

  return (
    <nav className={styles.sidebar__container}>
      <div className={styles.sidebar__icons}>
        {routes.map(({ name, path, icon }) => (
          <Link href={path} key={name}>
            <div className={iconClass(path, name)} onClick={() => handleClick(name)}>
              {icon}
            </div>
          </Link>
        ))}
      </div>
    </nav>
  );
}