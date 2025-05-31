"use client";

import Image from "next/image";
import styles from "./page.module.css";

type User = {
  id: string;
  name: string;
  joined: string;
  role: string;
};

type Props = {
  user: User;
};

export default function UserCard({ user }: Props) {
  return (
    <div className={styles.card}>
      <div className={styles.avatarContainer}>
        <Image
          src="/user-avatar.svg"
          alt="User Avatar"
          width={250}
          height={250}
          className={styles.avatar}
        />
      </div>
      <div className={styles.info}>
        <p className={styles.role}>{user.role}</p>
        <h2 className={styles.name}>{user.name}</h2>
        <p className={styles.detail}>
          <strong>ID:</strong> {user.id}
        </p>
        <p className={styles.detail}>
          <strong>Date Joined:</strong> {user.joined}
        </p>
        <p className={styles.detail}>
          <strong>Email:</strong> example@email.com
        </p>
      </div>
    </div>
  );
}
