# Renewo - Smart Subscription & Expense Manager

<p align="center">
  <img src="logo.svg" alt="Renewo Logo" width="120" />
</p>

<p align="center">
  <b>A modern, beautiful, and secure cross-platform subscription tracker and expense management application.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android%20%7C%20Desktop-3DDC84?style=flat-square&logo=android" alt="Platforms" />
  <img src="https://img.shields.io/badge/Kotlin%20Multiplatform-Compose-7F52FF?style=flat-square&logo=kotlin" alt="Kotlin Multiplatform" />
  <img src="https://img.shields.io/badge/Backend-Supabase-3ECF8E?style=flat-square&logo=supabase" alt="Supabase" />
  <img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square" alt="License" />
</p>

---

## ✨ Features

### 💳 Subscription Management
- **Recurring Payment Tracking:** Track subscriptions across daily, weekly, monthly, and yearly billing cycles with automated monthly and annual cost normalizations.
- **Quick Preset Selection:** Instant 1-tap addition for popular services (Netflix, Spotify, YouTube Premium, Amazon Prime, iCloud, Google One, Gym, ChatGPT Plus, GitHub, etc.) with pre-configured brand colors and logos.
- **Service Details & External Links:** Store custom notes, account credentials/splits, and directly launch service websites via built-in browser integration.
- **Active & Paused Controls:** Toggle subscriptions active or paused without deleting your payment records.
- **Smart Renewal Alerts:** Customizable push notifications and in-app alerts configurable 1 to 7 days before any billing event.

### 📊 Real-Time Analytics & Spending Insights
- **Weekly & Monthly Trajectory:** Interactive expenditure breakdown charting daily spending distribution, peak expenditure days, and average daily burn rate.
- **Dynamic Category Breakdown:** Visual progress indicators segmenting spend across Entertainment, Utilities, Cloud, Health & Fitness, Software, Education, and custom categories.
- **Visual Budgeting & Caps:** Set monthly budget thresholds with real-time visual progress bars and dynamic over-budget warnings.

### 🌍 Multi-Currency & Global Support
- **Live Currency Conversion:** Seamlessly switch between USD (`$`), EUR (`€`), GBP (`£`), INR (`₹`), JPY (`¥`), CAD (`CA$`), and AUD (`A$`).
- **Normalized Valuations:** All subscription costs automatically convert and re-calculate dynamically to match your active currency in real time.

### ⚡ Offline-First Architecture & Zero-Delay Launch
- **Instant Local Cache Hydration:** Zero-delay startup rendering powered by platform-native local storage. No blank loading screens or content flashes.
- **Bidirectional Cloud Sync:** Seamless background synchronization with Supabase PostgreSQL. Work offline and changes automatically sync when reconnected.

### 💎 Dual-Tier Plan System (Basic vs. Pro)
- **Basic Tier:**
  - Up to 10 active subscriptions.
  - Top category analytics and spending summaries.
  - Multi-currency conversions and renewal alerts.
- **Renewo Pro Tier:**
  - Track up to 200 subscriptions.
  - Full granular category analytics and peak-spend day indicators.
  - **Data Export:** Export your entire subscription portfolio and transaction log to CSV/JSON format.
  - High-visibility Pro status indicator.

### 🔒 Security & Data Privacy
- **Supabase Authentication:** Secure email/password login, Google OAuth integration, and automatic token refresh management.
- **Enterprise-Grade Sanitization:** Comprehensive client and database-level input validation preventing SQL injections, XSS, and payload overflow attacks.
- **Row-Level Security (RLS):** Supabase database tables protected with strict user isolation policies so only authenticated users access their own data.

### 🎨 Adaptive Design & Themes
- **Dark, Light & System Themes:** Crafted following modern mobile UI guidelines with rich contrast, smooth transitions, and high readability.
- **Custom Profile Avatars:** Personalize your account with custom display names and accent color selectors.

---

## 👨‍💻 Developer Credit

Developed and engineered by **Rahul Misal** ([@CodesRahul96](https://github.com/CodesRahul96)).

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
