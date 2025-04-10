import { redirect } from 'next/navigation';



export default function Home() {
  redirect('/authentication/login');
  // This line won't actually be reached
  return null;
}
