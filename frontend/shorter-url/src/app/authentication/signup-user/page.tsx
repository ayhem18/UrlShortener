import Image from "next/image";
import styles from "../page.module.css";
import Link from 'next/link';


export default function SignU() {
  return (
    <div className={styles.parentContainer}>
      <div className={styles.signup}>
        <p className={styles.page__title}>Create your account</p>
        <p className={styles.link__title}>Already have an account ? <span className={styles.link}> <Link href="/Authentification/signup-company">Log in</Link></span></p>
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
          <div className={styles.email}>
            <p className={styles.label}>Email</p>
            <input type="text" placeholder="Email" className={styles.input} />
          </div>
          <div className={styles.password}>
            <p className={styles.label}>Password</p>
            <input type="password" placeholder="Password" className={styles.input} />
          </div>
          <div className={styles.confirm_password}>
            <p className={styles.label}>Confirm Password</p>
            <input type="Password" placeholder="Password" className={styles.input} />
          </div>
          <div className={styles.token}>
            <p className={styles.label}>Access Token</p>
            <input type="text" placeholder="Token" className={styles.input} />
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
