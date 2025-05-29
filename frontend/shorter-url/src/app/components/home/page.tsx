"use client";

import { redirect } from 'next/navigation';
import styles from "./page.module.css";
import Image from "next/image";
import { useState } from "react";

export default function Home() {
  const [inputUrl, setInputUrl] = useState("");
  const [remaining, setRemaining] = useState(10); // To be changed
  const [error, setError] = useState("");
  type LinkItem = {
    url: string;
    time: string;
    date: string;
  };
  const [links, setLinks] = useState<LinkItem[]>([]);

  const handleShorten = () => {
    if (remaining == 0) {
      setError("You have no remaining links.");
      return;
    }

    if (!inputUrl.trim()) {
      setError("Invalid Domain");
      return;
    }

    setError("");

    const now = new Date();
    const newLink = {
      url: inputUrl,
      time: now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      date: now.toLocaleDateString(),
    };
    setLinks(prev => [newLink, ...prev]);
    setRemaining(prev => prev - 1);
    setInputUrl("");
  };
  const handleClear = () => {
    setLinks([]);
  };
  const showError = error !== "";

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
      <div className={styles.home_components}>
        <div className={styles.description}>
          <p className={styles.desc_title}>Build Stronger Digital Connections</p>
          <div className={styles.desc}>
            <p>User our URL shortener to engage your audience and connect them to the right information.</p>
            <p>Build, Edit, and Track everything inside the <span className={styles.desc_shorter}>Shorter<span className={styles.desc_url}>.Url</span></span> connections platform</p>
          </div>
        </div>
        <div className={styles.inputBox}>
          <div className={styles.inputGroup}>
            <label className={styles.inputLabel}>Paste your link here:</label>
            <p className={styles.urlCount}>Remaining: {remaining}</p>
            <input
              id="urlInput"
              type="text"
              className={`${styles.inputField} ${showError ? styles.inputError : ""}`}
              value={inputUrl}
              onChange={(e) => setInputUrl(e.target.value)}
            />
            <p className={styles.errorMessage}>
              {showError ? error : <span style={{ visibility: "hidden" }}>placeholder</span>}
            </p>

            <button onClick={handleShorten}
              className={styles.inputBtn}
            >
              <span className={styles.btnContent}>
                <span className={styles.btnText}>Get your link</span>
                <Image
                  src="/circle-arrow.svg"
                  alt="Arrow icon"
                  width={18}
                  height={18}
                  className={styles.arrowIcon}
                />
              </span>
            </button>
          </div>
        </div>
        <div className={styles.history}>
          {links.length === 0 ? (
            <p className={styles.nolink}>No links have been generated yet</p>
          ) : (
            links.map((link, index) => (
              <div key={index} className={styles.linkRow}>
                <p className={styles.time}>{link.time}</p>
                <p className={styles.links}>{link.url}</p>
                <p className={styles.date}>{link.date}</p>
              </div>
            ))
          )}
        </div>
        <div className={styles.clearBtnWrapper}>
          <button
            className={styles.clearBtn}
            onClick={handleClear}
            style={{ visibility: links.length > 0 ? "visible" : "hidden" }}
          >
            Clear
          </button>
        </div>
      </div>
    </div>
  );
}