import Image from "next/image";
import styles from "../page.module.css";
import Link from 'next/link';


export default function SignOPage() {
  return (
    <div className={styles.parentContainer}>
      <div className={styles.signup}>
        <p className={styles.page__title}>Register your company</p>
        <p className={styles.link__title}>Already have an account ? <span className={styles.link}> <Link href="/Authentification/login">Log in</Link></span></p>
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
        <div className={styles.inputs}>
          <div className={styles.name__inputs}>
            <div className={styles.name}>
              <p className={styles.label}>Name</p>
              <input type="text" placeholder="Name" className={styles.name__input} />
            </div>
            <div className={styles.surname}>
              <p className={styles.label}>Surname</p>
              <input type="text" placeholder="Surname" className={styles.name__input} />
            </div>
            <div className={styles.middlename}>
              <p className={styles.label}>Middle Name</p>
              <input type="text" placeholder="Middle Name" className={styles.name__input} />
            </div>
          </div>
          <div className={styles.company__inputs}>
            <div className={styles.email}>
              <p className={styles.label}>Email</p>
              <input type="text" placeholder="Email" className={styles.company__input} />
            </div>
            <div className={styles.password}>
              <p className={styles.label}>Password</p>
              <input type="password" placeholder="Password" className={styles.company__input} />
            </div>
          </div>
          <div className={styles.company__inputs}>
            <div className={styles.confirm_password}>
              <p className={styles.label}>Company</p>
              <input type="text" placeholder="Company" className={styles.company__input} />
            </div>
            <div className={styles.token}>
              <p className={styles.label}>Company Address</p>
              <input type="text" placeholder="Company Address" className={styles.company__input} />
            </div>
          </div>
          <div className={styles.company__inputs}>
            <div className={styles.confirm_password}>
              <p className={styles.label}>Company Domain</p>
              <input type="text" placeholder="Company Domain" className={styles.company__input} />
            </div>
            <div className={styles.token}>
              <p className={styles.label}>Subscription</p>
              <select className={styles.tier__input}   defaultValue="" required>
                <option value=""   hidden className={styles.disabled__tier}> Subscription</option>
                <option value="1" className={styles.select__tier}>Free Tier</option>
                <option value="2" className={styles.select__tier}>Tier One</option>
                <option value="3" className={styles.select__itier}>Tier Infinity</option>
              </select>
            </div>
          </div>
        </div>
        <button type="submit" className={styles.submit__btn}>Create account</button>
        <div className={styles.terms__container__signup}>
          <p className={styles.terms}>By logging in with an account, you agree to Shorter.url&apos;s <a href="https://www.youtube.com/watch?v=dQw4w9WgXcQ" className={styles.important}>Terms of service</a>, <a href="https://www.youtube.com/watch?v=dQw4w9WgXcQ" className={styles.important}>Privacy Policy</a> and <a href="https://www.youtube.com/watch?v=dQw4w9WgXcQ" className={styles.important}>Acceptable Use Policy</a></p>
        </div>
      </div>
    </div>
  );
}
