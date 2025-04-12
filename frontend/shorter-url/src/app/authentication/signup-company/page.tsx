"use client";

import Image from "next/image";
import styles from "../page.module.css";
import Link from 'next/link';
import { useState } from 'react';


export default function SignOPage() {
  const [formData, setFormData] = useState({
    name: '',
    surname: '',
    middlename: '',
    email: '',
    password: '',
    company: '',
    companyAddress: '',
    companyDomain: '',
    subscription: ''
  });

  const [errors, setErrors] = useState({
    name: false,
    surname: false,
    email: false,
    password: false,
    company: false,
    companyAddress: false,
    companyDomain: false,
    subscription: false
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    if (errors[name as keyof typeof errors]) {
      setErrors(prev => ({
        ...prev,
        [name]: false
      }));
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    const newErrors = {
      name: formData.name.trim() === '',
      surname: formData.surname.trim() === '',
      email: formData.email.trim() === '',
      password: formData.password.trim() === '',
      company: formData.company.trim() === '',
      companyAddress: formData.companyAddress.trim() === '',
      companyDomain: formData.companyDomain.trim() === '',
      subscription: formData.subscription.trim() === ''
    };

    setErrors(newErrors);
    
    if (!Object.values(newErrors).some(error => error)) {
      console.log('Registering company with:', formData);
    }
  };
  return (
    <div className={styles.parentContainer}>
      <div className={styles.signup}>
        <p className={styles.page__title}>Register your company</p>
        <p className={styles.link__title}>Already have an account ? <span className={styles.link}> <Link href="/authentication/login">Log in</Link></span></p>
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
          <div className={styles.name__inputs}>
            <div className={styles.name}>
              <p className={styles.label}>Name</p>
              <input type="text" name="name" placeholder="Name" className={`${styles.name__input} ${errors.name ? styles.error : ''}`} value={formData.name} onChange={handleChange}/>
              {errors.name && <p className={styles.errorMessage}>Name is required</p>}
            </div>
            <div className={styles.surname}>
              <p className={styles.label}>Surname</p>
              <input type="text" name="surname" placeholder="Surname" className={`${styles.name__input} ${errors.surname ? styles.error : ''}`} value={formData.surname} onChange={handleChange}/>
              {errors.surname && <p className={styles.errorMessage}>Surname is required</p>}
            </div>
            <div className={styles.middlename}>
              <p className={styles.label}>Middle Name</p>
              <input type="text" name="middlename" placeholder="Middle Name" className={styles.name__input} value={formData.middlename} onChange={handleChange} />
            </div>
          </div>
          <div className={styles.company__inputs}>
            <div className={styles.email}>
              <p className={styles.label}>Email</p>
              <input type="text" name="email" placeholder="Email" className={`${styles.company__input} ${errors.email ? styles.error : ''}`} value={formData.email} onChange={handleChange}/>
              {errors.email && <p className={styles.errorMessage}>Email is required</p>}
            </div>
            <div className={styles.password}>
              <p className={styles.label}>Password</p>
              <input type="password" name="password" placeholder="Password" className={`${styles.company__input} ${errors.password ? styles.error : ''}`} value={formData.password} onChange={handleChange}/>
              {errors.password && <p className={styles.errorMessage}>Password is required</p>}
            </div>
          </div>
          <div className={styles.company__inputs}>
            <div className={styles.confirm_password}>
              <p className={styles.label}>Company</p>
              <input type="text" name="company" placeholder="Company" className={`${styles.company__input} ${errors.company ? styles.error : ''}`} value={formData.company} onChange={handleChange}/>
              {errors.company && <p className={styles.errorMessage}>Company name is required</p>}
            </div>
            <div className={styles.token}>
              <p className={styles.label}>Company Address</p>
              <input type="text" name="companyAddress" placeholder="Company Address" className={`${styles.company__input} ${errors.companyAddress ? styles.error : ''}`} value={formData.companyAddress} onChange={handleChange}/>
              {errors.companyAddress && <p className={styles.errorMessage}>Company address is required</p>}
            </div>
          </div>
          <div className={styles.company__inputs}>
            <div className={styles.confirm_password}>
              <p className={styles.label}>Company Domain</p>
              <input type="text" name="companyDomain" placeholder="Company Domain" className={`${styles.company__input} ${errors.companyDomain ? styles.error : ''}`} value={formData.companyDomain} onChange={handleChange}/>
              {errors.companyDomain && <p className={styles.errorMessage}>Company domain is required</p>}
            </div>
            <div className={styles.token}>
              <p className={styles.label}>Subscription</p>
              <select className={`${styles.tier__input} ${errors.subscription ? styles.error : ''}`} name="subscription"  defaultValue="0" required value={formData.subscription} onChange={handleChange}>
                <option value="0"   hidden className={styles.disabled__tier}> Subscription</option>
                <option value="1" className={styles.select__tier}>Free Tier</option>
                <option value="2" className={styles.select__tier}>Tier One</option>
                <option value="3" className={styles.select__itier}>Tier Infinity</option>
              </select>
              {errors.subscription && <p className={styles.errorMessage}>Please select a subscription tier</p>}
            </div>
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
