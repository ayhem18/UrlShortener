"use client";

import { redirect } from 'next/navigation';
import styles from "./page.module.css";
import Image from "next/image";
import { useState, useRef, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import UserCard from "@/app/components/dashboard/usercard/page";

type User = {
  id: string;
  name: string;
  joined: string;
  role: string;
};

const data: User[] = [
  { id: "#44122", name: "F.Khlifi", joined: "24/10/2022", role: "Owner" },
  { id: "#58825", name: "M.Laabidi", joined: "17/10/2022", role: "Admin" },
  { id: "#73003", name: "W.Dineri", joined: "04/10/2022", role: "Admin" },
  { id: "#20462", name: "A.Allimi", joined: "13/05/2022", role: "Employee" },
  { id: "#18933", name: "A.Bouabid", joined: "22/05/2022", role: "Employee" },
  { id: "#45169", name: "M.Mrabit", joined: "15/06/2022", role: "Employee" },
  { id: "#34304", name: "S.Weslati", joined: "06/09/2022", role: "Employee" },
  { id: "#17188", name: "H.Trabelsi", joined: "25/09/2022", role: "Employee" },
];

export default function DashboardHome() {
  const [activeCategory, setActiveCategory] = useState<string>("all");
  const [containerHeight, setContainerHeight] = useState<number | "auto">("auto");
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const wrapperRef = useRef<HTMLDivElement>(null);

  const categories = [
    { label: `Employees:${data.filter(u => u.role === "Employee").length}/${data.length}`, key: "employees" },
    { label: `Admins:${data.filter(u => u.role === "Admin").length}/3`, key: "admins" },
    { label: "Owner:You", key: "owner" },
    { label: "All", key: "all" },
  ];

  const filteredData = data.filter((user) => {
    if (activeCategory === "all") return true;
    if (activeCategory === "employees") return user.role === "Employee";
    if (activeCategory === "admins") return user.role === "Admin";
    if (activeCategory === "owner") return user.role === "Owner";
    return false;
  });

  useEffect(() => {
    if (wrapperRef.current) {
      setContainerHeight(wrapperRef.current.scrollHeight);
    }
  }, [filteredData]);
  return (
    <div className={styles.bg_style}>
  <div className={styles.mainLayout}>
    <div className={styles.info}>
      <div className={styles.infoBox}>
        <p className={styles.company__name}>Company: Innopolis University</p>
        <div className={styles.sort__box}>
          {categories.map((category) => (
            <div
              key={category.key}
              className={`${styles.category} ${activeCategory === category.key ? styles.active : ""}`}
              onClick={() => setActiveCategory(category.key)}
            >
              <p>{category.label}</p>
            </div>
          ))}
        </div>
      </div>

      <motion.div
        className={styles.tableWrapper}
        ref={wrapperRef}
        animate={{ height: containerHeight }}
        style={{ overflow: "hidden" }}
        transition={{ type: "spring", stiffness: 70, damping: 15 }}
      >
        <motion.table className={styles.dataTable} layout>
          <thead>
            <tr>
              <th>Identifier</th>
              <th>Name</th>
              <th>Date Joined</th>
              <th>Role</th>
            </tr>
          </thead>
          <motion.tbody layout>
            <AnimatePresence initial={false}>
              {filteredData.map((user) => (
                <motion.tr
                  key={user.id}
                  onClick={() => setSelectedUser(user)}
                  layout
                  initial={{ opacity: 0, y: 20, scale: 0.98 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, y: -20, scale: 0.95 }}
                  transition={{ type: "spring", stiffness: 70, damping: 15 }}
                  className={styles.clickableRow}
                >
                  <td>{user.id}</td>
                  <td>{user.name}</td>
                  <td>{user.joined}</td>
                  <td>{user.role}</td>
                </motion.tr>
              ))}
            </AnimatePresence>
          </motion.tbody>
        </motion.table>
      </motion.div>
    </div>
    {selectedUser && (
      <div className={styles.userCardWrapper}>
        <UserCard user={selectedUser} />
      </div>
    )}
  </div>
</div>
  );
}