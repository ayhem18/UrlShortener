import { ReactNode } from 'react';
import Image from "next/image";
import styles from "./page.module.css";
import Sidebar from "@/app/components/dashboard/sidebar/page";

export default function DashboardLayout({ children }: { children: ReactNode }) {
  return (
<div className={styles.dashboardWrapper}>
      <Sidebar />
      <main className={styles.mainContent}>
        {children}
      </main>
    </div>
  );
}
