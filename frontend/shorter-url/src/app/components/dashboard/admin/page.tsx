"use client";

import { redirect } from 'next/navigation';
import styles from "./page.module.css";
import Image from "next/image";
import { useState, useRef, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import CopyIcon from '@/assets/copy.svg';
import UserCard from "@/app/components/dashboard/usercard/page";

type User = {
  id: string;
  name: string;
  joined: string;
  role: string;
  token: string;
};

const data: User[] = [
  { id: "#44122", name: "F.Khlifi", joined: "24/10/2022", role: "Owner", token: "27966f223566ae5d8961f1" },
  { id: "#58825", name: "M.Laabidi", joined: "17/10/2022", role: "Admin", token: "27966f223566ae5d8961f1" },
  { id: "#73003", name: "W.Dineri", joined: "04/10/2022", role: "Admin", token: "27966f223566ae5d8961f1" },
  { id: "#20462", name: "A.Allimi", joined: "13/05/2022", role: "Employee", token: "27966f223566ae5d8961f1" },
  { id: "#18933", name: "A.Bouabid", joined: "22/05/2022", role: "Employee", token: "27966f223566ae5d8961f1" },
  { id: "#45169", name: "M.Mrabit", joined: "15/06/2022", role: "Employee", token: "27966f223566ae5d8961f1" },
  { id: "#34304", name: "S.Weslati", joined: "06/09/2022", role: "Employee", token: "27966f223566ae5d8961f1" },
  { id: "#17188", name: "H.Trabelsi", joined: "25/09/2022", role: "Employee", token: "27966f223566ae5d8961f1" },
];

export default function DashboardAdmin() {
  const [activeCategory, setActiveCategory] = useState<string>("all");
  const [containerHeight, setContainerHeight] = useState<number | "auto">("auto");
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [users, setUsers] = useState<User[]>(data);
  const [showSuccessMessage, setShowSuccessMessage] = useState(false);
  const [copied, setCopied] = useState(false);
  const wrapperRef = useRef<HTMLDivElement>(null);

  const categories = [
    { label: `Employees:${data.filter(u => u.role === "Employee").length}/${users.length}`, key: "employees" },
    { label: `Admins:${data.filter(u => u.role === "Admin").length}/3`, key: "admins" },
    { label: "Owner:You", key: "owner" },
    { label: "All", key: "all" },
  ];

  const filteredData = users.filter((user) => {
    if (activeCategory === "all") return true;
    if (activeCategory === "employees") return user.role === "Employee";
    if (activeCategory === "admins") return user.role === "Admin";
    if (activeCategory === "owner") return user.role === "Owner";
    return false;
  });
  const deleteUser = (id: string) => {
    setUsers(prev => prev.filter(user => user.id !== id));
    if (selectedUser?.id === id) {
      setSelectedUser(null);
    }
  };

  const handleGenerate = () => {
    setShowSuccessMessage(true);
    setTimeout(() => setShowSuccessMessage(false), 3000);
  };
  useEffect(() => {
    if (wrapperRef.current) {
      setContainerHeight(wrapperRef.current.scrollHeight);
    }
  }, [filteredData]);

  const handleCopy = (token: string) => {
    navigator.clipboard.writeText(token)
    setCopied(true);
    setTimeout(() => setCopied(false), 1000);
  };
  return (
    <div className={styles.bg_style}>
      <div className={styles.mainLayout}>
        <div className={styles.info}>
          <div className={styles.infoBox}>
            <p className={styles.title__name}>Manage your tokens</p>
            <div className={styles.token__field}>
              <div className={styles.token__message}>
                <div className={styles.token}>
                  <p>{data[0].token}</p>
                  <div className={styles.copy__wrapper}>
                    <CopyIcon onClick={() => handleCopy(data[0].token)}
                      className={styles.copy__icon}></CopyIcon>
                    <span className={styles.tooltip}>{copied ? "Copied" : "Copy"}</span>
                  </div>

                </div>
                {showSuccessMessage && (
                  <p className={styles.success_message}>Token generated successfully.</p>
                )}              </div>

              <div className={styles.formRow}>

                <select className={styles.styled__select} defaultValue="">
                  <option value="" disabled>Select Role</option>
                  <option value="employee">Employee</option>
                  <option value="admin">Admin</option>
                  <option value="owner">Owner</option>
                </select>
                <button className={styles.generateBtn} onClick={handleGenerate}>Generate</button>
              </div>
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
                  <th>Token</th>
                  <th>Action</th>
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
                      <td>{user.token}</td>
                      <td onClick={(e) => {
                        e.stopPropagation();
                        deleteUser(user.id);
                      }}>
                        <Image
                          src="/trash.svg"
                          alt="Trash icon"
                          width={24}
                          height={24}
                          style={{ cursor: "pointer" }}
                        />
                      </td>
                    </motion.tr>
                  ))}
                </AnimatePresence>
              </motion.tbody>
            </motion.table>
          </motion.div>
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
        {selectedUser && (
          <div className={styles.userCardWrapper}>
            <UserCard user={selectedUser} />
          </div>
        )}
      </div>
    </div>
  );
}