"use client"; // Mark this as a client component

import { usePathname } from "next/navigation";
import "./globals.css";
import Navbar from "@/app/components/navbar/page";

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const pathname = usePathname();

  const noNavbarRoutes = ["/authentication/signup-company", "/authentication/signup-user", "/authentication/login"];

  const showNavbar = !noNavbarRoutes.includes(pathname);

  return (
    <html lang="en">
      <body>
        {showNavbar && <Navbar />}
        <main>{children}</main>
      </body>
    </html>
  );
}
