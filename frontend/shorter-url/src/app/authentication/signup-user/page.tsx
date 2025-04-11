"use client";

import Image from "next/image";
import styles from "../page.module.css";
import Link from 'next/link';
import { useState } from 'react';


export default function SignU() {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    confirmPassword: '',
    token: ''
  });

  const [errors, setErrors] = useState({
    email: false,
    password: false,
    confirmPassword: false,
    token: false,
    passwordMatch: false
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    // Clear error when user types
    if (errors[name as keyof typeof errors]) {
      setErrors(prev => ({
        ...prev,
        [name]: false,
        passwordMatch: name === 'password' || name === 'confirmPassword' ? false : prev.passwordMatch
      }));
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const newErrors = {
      email: formData.email.trim() === '',
      password: formData.password.trim() === '',
      confirmPassword: formData.confirmPassword.trim() === '',
      token: formData.token.trim() === '',
      passwordMatch: formData.password !== formData.confirmPassword
    };

    setErrors(newErrors);

    if (!newErrors.email && !newErrors.password && !newErrors.confirmPassword &&
      !newErrors.token && !newErrors.passwordMatch) {
      console.log('Signing up with:', formData);
    }
  };
  return (
    <div className={styles.parentContainer}>
      <div className={styles.signup}>
        <p className={styles.page__title}>Create your account</p>
        <p className={styles.link__title}>Already have an account ? <span className={styles.link}> <Link href="/authentication/signup-company">Log in</Link></span></p>
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
        <form onSubmit={handleSubmit} className={styles.form}>
        <div className={styles.inputs}>
          <div className={styles.email}>
            <p className={styles.label}>Email</p>
            <input type="text" name="email" placeholder="Email" className={`${styles.input} ${errors.email ? styles.error : ''}`} value={formData.email} onChange={handleChange}/>
            {errors.email && <p className={styles.errorMessage}>Email is required</p>}
          </div>
          <div className={styles.password}>
            <p className={styles.label}>Password</p>
            <input type="password" name="password" placeholder="Password" className={`${styles.input} ${errors.password ? styles.error : ''}`} value={formData.password} onChange={handleChange} />
            {errors.password && <p className={styles.errorMessage}>Password is required</p>}
          </div>
          <div className={styles.confirm_password}>
            <p className={styles.label}>Confirm Password</p>
            <input type="Password" name="confirmPassword" placeholder="Password" className={`${styles.input} ${errors.confirmPassword || errors.passwordMatch ? styles.error : ''}`} value={formData.confirmPassword} onChange={handleChange} />
            {errors.confirmPassword && <p className={styles.errorMessage}>Please confirm your password</p>}
              {errors.passwordMatch && <p className={styles.errorMessage}>Passwords don&apos;t match</p>}
          </div>
          <div className={styles.token}>
            <p className={styles.label}>Access Token</p>
            <input type="text" name="token" placeholder="Token" className={`${styles.input} ${errors.token ? styles.error : ''}`} value={formData.token} onChange={handleChange} />
            {errors.token && <p className={styles.errorMessage}>Token is required</p>}
          </div>
        </div>
        <button type="submit" className={styles.submit__btn}>Create account</button>
        </form>
        <div className={styles.terms__container__signup}>
          <p className={styles.terms}>By logging in with an account, you agree to Shorter.url&apos;s <a href="https://www.youtube.com/watch?v=dQw4w9WgXcQ" className={styles.important}>Terms of service</a>, <a href="https://www.youtube.com/watch?v=dQw4w9WgXcQ" className={styles.important}>Privacy Policy</a> and <a href="https://www.youtube.com/watch?v=dQw4w9WgXcQ" className={styles.important}>Acceptable Use Policy</a></p>
        </div>
      </div>
    </div>
  );
}
