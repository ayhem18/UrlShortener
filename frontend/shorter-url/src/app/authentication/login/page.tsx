"use client";

import Image from "next/image";
import styles from "../page.module.css";
import Link from 'next/link';
import { useState } from 'react';


export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState({
    email: false,
    password: false
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const newErrors = {
      email: email.trim() === '',
      password: password.trim() === ''
    };

    setErrors(newErrors);

    if (!newErrors.email && !newErrors.password) {
      console.log('Logging in with:', email, password);
    }
  };

  return (
    <div className={styles.parentContainer}>
      <div className={styles.login}>
        <p className={styles.page__title}>Log in with you company account</p>
        <p className={styles.link__title}>Don&apos;t have an account ? <span className={styles.link}><Link href="/authentication/signup-user">Sign up</Link></span></p>
        <div className={styles.ssoButtons}>
          <Image
            src="/yandex.svg"
            alt="Yandex Login"
            width={600}
            height={54}
            className={styles.ssoButton}
          />
          <Image
            src="/google.svg"
            alt="google Login"
            width={600}
            height={54}
            className={styles.ssoButton}
          />
        </div>
        <p className={styles.or}>OR</p>
        <form onSubmit={handleSubmit} className={styles.inputs}>
          <div className={styles.inputs}>
            <div className={styles.email}>
              <p className={styles.label}>Email</p>
              <input type="text" value={email} placeholder="Email" onChange={(e) => setEmail(e.target.value)} className={`${styles.input} ${errors.email ? styles.error : ''}`} />
              {errors.email && <p className={styles.errorMessage}>Email is required</p>}
            </div>
            <div className={styles.password}>
              <p className={styles.label}>Password</p>
              <input type="password" value={password} placeholder="Password" onChange={(e) => setPassword(e.target.value)} className={`${styles.input} ${errors.password ? styles.error : ''}`} />
              {errors.password && <p className={styles.errorMessage}>Password is required</p>}
              <p className={styles.forgot}>Forgot your password?</p>
            </div>
          </div>
          <button type="submit" className={styles.submit__btn}>Log in</button>
          <div className={styles.terms__container__login}>
            <p className={styles.terms}>By logging in with an account, you agree to Shorter.url&apos;s <a href="https://www.youtube.com/watch?v=dQw4w9WgXcQ" className={styles.important}>Terms of service</a>, <a href="https://www.youtube.com/watch?v=dQw4w9WgXcQ" className={styles.important}>Privacy Policy</a> and <a href="https://www.youtube.com/watch?v=dQw4w9WgXcQ" className={styles.important}>Acceptable Use Policy</a></p>
          </div>
        </form>
      </div>
    </div>
  );
}
