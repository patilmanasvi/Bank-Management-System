const pptxgen = require('pptxgenjs');

const pres = new pptxgen();
pres.layout = 'LAYOUT_16x9'; // 13.333 x 7.5 inches
pres.author = 'Bank Management System Team';
pres.company = 'Computer Engineering Dept';
pres.title = 'Bank Management System - Project Presentation';

// Color Palette
const C_NAVY = '0B2545';
const C_NAVY_LIGHT = '13345A';
const C_BLUE = '1D4E89';
const C_ACCENT = '2A6FC9';
const C_BG_LIGHT = 'F4F6F9';
const C_CARD_BG = 'FFFFFF';
const C_BORDER = 'D1D9E2';
const C_BORDER_ACCENT = 'B0C4DE';
const C_TEXT_DARK = '0B2545';
const C_TEXT_MUTED = '5B6B7E';
const C_WHITE = 'FFFFFF';
const C_GREEN = '137333';
const C_GREEN_BG = 'E6F4EA';
const C_RED = 'C5221F';
const C_RED_BG = 'FCE8E6';
const C_TAG_BG = 'E8F0FE';

// Helper: Standard Slide Header with Section Badge
function addHeader(slide, title, category, subtitle) {
  // Top header bar
  slide.addShape(pres.ShapeType.rect, {
    x: 0, y: 0, w: 13.333, h: 1.05,
    fill: { color: C_NAVY }
  });

  // Section Tag / Category Badge
  if (category) {
    slide.addShape(pres.ShapeType.roundRect, {
      x: 0.8, y: 0.12, w: 2.2, h: 0.22, rectRadius: 0.08,
      fill: { color: C_ACCENT }, line: { color: C_ACCENT }
    });
    slide.addText(category.toUpperCase(), {
      x: 0.8, y: 0.12, w: 2.2, h: 0.22,
      fontSize: 8.5, fontFace: 'Segoe UI', bold: true, color: C_WHITE, align: 'center', valign: 'middle'
    });
  }

  // Slide Title
  slide.addText(title, {
    x: 0.8, y: category ? 0.36 : 0.2, w: 11.7, h: 0.42,
    fontSize: 18, fontFace: 'Segoe UI', bold: true, color: C_WHITE
  });

  // Subtitle
  if (subtitle) {
    slide.addText(subtitle, {
      x: 0.8, y: 0.76, w: 11.7, h: 0.22,
      fontSize: 10.5, fontFace: 'Segoe UI', color: 'B0C8E8'
    });
  }
}

// Helper: Add Stylized Image / Screenshot Container
function addImageFrame(slide, x, y, w, h, label, note) {
  // Outer frame container
  slide.addShape(pres.ShapeType.roundRect, {
    x: x, y: y, w: w, h: h, rectRadius: 0.08,
    fill: { color: 'EAEEF3' }, line: { color: 'B8C7D9', width: 1.5, dashType: 'dash' }
  });

  // Inner center placeholder icon/box
  const boxW = Math.min(w - 0.6, 4.2);
  const boxH = 1.0;
  slide.addShape(pres.ShapeType.roundRect, {
    x: x + (w - boxW) / 2, y: y + (h - boxH) / 2 - 0.15, w: boxW, h: boxH, rectRadius: 0.06,
    fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
  });

  slide.addText(label || 'INSERT SCREENSHOT HERE', {
    x: x + (w - boxW) / 2, y: y + (h - boxH) / 2 - 0.1, w: boxW, h: 0.45,
    fontSize: 11, fontFace: 'Segoe UI', bold: true, color: C_BLUE, align: 'center'
  });

  slide.addText(note || 'Paste website or MySQL screenshot inside this frame', {
    x: x + (w - boxW) / 2, y: y + (h - boxH) / 2 + 0.3, w: boxW, h: 0.45,
    fontSize: 9, fontFace: 'Segoe UI', color: C_TEXT_MUTED, align: 'center'
  });
}

// ==========================================
// SLIDE 1: Title Slide (Dark Premium Theme)
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_NAVY };

  // Decorative Accent bar
  slide.addShape(pres.ShapeType.roundRect, {
    x: 0.8, y: 1.1, w: 2.6, h: 0.32, rectRadius: 0.1,
    fill: { color: C_ACCENT }
  });
  slide.addText('CAPSTONE PROJECT 2026', {
    x: 0.8, y: 1.1, w: 2.6, h: 0.32,
    fontSize: 10, fontFace: 'Segoe UI', bold: true, color: C_WHITE, align: 'center', valign: 'middle'
  });

  // Main Title
  slide.addText('Bank Management System', {
    x: 0.8, y: 1.6, w: 11.5, h: 0.9,
    fontSize: 34, fontFace: 'Segoe UI', bold: true, color: C_WHITE
  });

  // Subtitle
  slide.addText('An Enterprise 3-Tier Banking System Demonstrating Advanced OOP, 3NF Relational DBMS & ACID Transactions', {
    x: 0.8, y: 2.55, w: 11.5, h: 0.55,
    fontSize: 13.5, fontFace: 'Segoe UI', color: 'C8DCF2'
  });

  // Left Card: Project Metadata
  slide.addShape(pres.ShapeType.roundRect, {
    x: 0.8, y: 3.4, w: 6.8, h: 3.4, rectRadius: 0.1,
    fill: { color: C_NAVY_LIGHT }, line: { color: '244B78', width: 1 }
  });
  slide.addText('Project Overview & Architecture', {
    x: 1.1, y: 3.6, w: 6.2, h: 0.35,
    fontSize: 13, fontFace: 'Segoe UI', bold: true, color: '89B6E8'
  });
  slide.addText([
    { text: 'Course: ', options: { bold: true, color: C_WHITE } },
    { text: 'OOP in Java & Database Management Systems Lab\n', options: { color: 'D8E6F5' } },
    { text: 'Tech Stack: ', options: { bold: true, color: C_WHITE } },
    { text: 'Java 11+ (REST Server), MySQL 8.0 (InnoDB), HTML5/CSS3/JS\n', options: { color: 'D8E6F5' } },
    { text: 'Core Capabilities: ', options: { bold: true, color: C_WHITE } },
    { text: 'ACID Fund Transfers, 3NF Schema (12 Tables), Role-Based Access Control, Account Freeze, KYC Profile & Loan Approvals', options: { color: 'D8E6F5' } }
  ], {
    x: 1.1, y: 4.05, w: 6.2, h: 2.5,
    fontSize: 10.5, fontFace: 'Segoe UI', lineSpacingMultiple: 1.2
  });

  // Right Frame: Hero Image / Logo Space
  addImageFrame(slide, 7.9, 3.4, 4.6, 3.4, 'PROJECT LOGO / UI PREVIEW', 'Insert College Logo or Dashboard Mockup');
}

// ==========================================
// SLIDE 2: Problem Statement & Objectives
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addHeader(slide, 'Problem Statement & Project Objectives', 'System Goals', 'Overcoming architectural limitations in traditional banking software');

  // Left Card: Traditional Problems
  slide.addShape(pres.ShapeType.roundRect, {
    x: 0.8, y: 1.25, w: 5.6, h: 5.6, rectRadius: 0.08,
    fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
  });
  slide.addShape(pres.ShapeType.rect, { x: 0.8, y: 1.25, w: 5.6, h: 0.45, fill: { color: C_RED } });
  slide.addText('CHALLENGES IN TRADITIONAL SOFTWARE', {
    x: 1.0, y: 1.3, w: 5.2, h: 0.35, fontSize: 11, fontFace: 'Segoe UI', bold: true, color: C_WHITE
  });
  slide.addText([
    { text: 'High UI-Database Coupling:\n', options: { bold: true, color: C_RED } },
    { text: 'Hardcoded SQL inside UI buttons leads to fragile code, SQL injection risks, and poor maintainability.\n\n' },
    { text: 'Concurrency & Race Conditions:\n', options: { bold: true, color: C_RED } },
    { text: 'Without transaction locks, simultaneous transfers cause dirty reads, lost updates, and phantom balances.\n\n' },
    { text: 'Unnormalized Data Schemas:\n', options: { bold: true, color: C_RED } },
    { text: 'Redundant branch and customer attributes create update and deletion anomalies.' }
  ], {
    x: 1.1, y: 1.9, w: 5.0, h: 4.8, fontSize: 10.5, fontFace: 'Segoe UI', color: C_TEXT_DARK
  });

  // Right Card: Project Objectives
  slide.addShape(pres.ShapeType.roundRect, {
    x: 6.8, y: 1.25, w: 5.7, h: 5.6, rectRadius: 0.08,
    fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
  });
  slide.addShape(pres.ShapeType.rect, { x: 6.8, y: 1.25, w: 5.7, h: 0.45, fill: { color: C_GREEN } });
  slide.addText('OUR CORE PROJECT OBJECTIVES', {
    x: 7.0, y: 1.3, w: 5.3, h: 0.35, fontSize: 11, fontFace: 'Segoe UI', bold: true, color: C_WHITE
  });
  slide.addText([
    { text: 'Decoupled 3-Tier Layering:\n', options: { bold: true, color: C_GREEN } },
    { text: 'Clean separation: Browser UI <-> Java REST Backend <-> MySQL 8.0 InnoDB.\n\n' },
    { text: 'Guaranteed ACID Transactions:\n', options: { bold: true, color: C_GREEN } },
    { text: 'Atomic all-or-nothing transfers with automated rollback on failure.\n\n' },
    { text: 'Strict 3NF Normalization:\n', options: { bold: true, color: C_GREEN } },
    { text: '12 relational tables with zero transitive dependencies.\n\n' },
    { text: 'Role-Based Access Control (RBAC):\n', options: { bold: true, color: C_GREEN } },
    { text: 'Fine-grained permissions for Customers, Staff, and Administrators.' }
  ], {
    x: 7.1, y: 1.9, w: 5.1, h: 4.8, fontSize: 10.5, fontFace: 'Segoe UI', color: C_TEXT_DARK
  });
}

// ==========================================
// SLIDE 3: 3-Tier System Architecture (Split with Diagram Frame)
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addHeader(slide, '3-Tier System Architecture & Data Flow', 'Architecture', 'End-to-end decoupled request/response pipeline');

  // Left Side: 3 Layer Cards
  const layers = [
    { title: '1. Presentation Layer', desc: 'Vanilla HTML5 / CSS3 / ES6 JS Web Portal + Swing GUI.\nCommunicates via async REST JSON calls (fetch API).', color: C_BLUE, y: 1.25 },
    { title: '2. Application Layer (Java Backend)', desc: 'Embedded Java HttpServer (Port 8080) with REST Handlers.\nOOP Domain Models & Services (Transaction, Account, Loan).', color: C_ACCENT, y: 3.15 },
    { title: '3. Database Layer (MySQL 8.0 InnoDB)', desc: '12 Relational Tables in 3NF Normalization.\nEnforces Foreign Keys, CHECK constraints, and ACID locks.', color: C_NAVY, y: 5.05 }
  ];

  layers.forEach(l => {
    slide.addShape(pres.ShapeType.roundRect, {
      x: 0.8, y: l.y, w: 5.8, h: 1.7, rectRadius: 0.08,
      fill: { color: C_CARD_BG }, line: { color: l.color, width: 1.5 }
    });
    slide.addShape(pres.ShapeType.rect, { x: 0.8, y: l.y, w: 0.15, h: 1.7, fill: { color: l.color } });
    slide.addText(l.title, {
      x: 1.1, y: l.y + 0.12, w: 5.3, h: 0.35, fontSize: 12.5, fontFace: 'Segoe UI', bold: true, color: l.color
    });
    slide.addText(l.desc, {
      x: 1.1, y: l.y + 0.5, w: 5.3, h: 1.1, fontSize: 10, fontFace: 'Segoe UI', color: C_TEXT_DARK
    });
  });

  // Right Side: Screenshot/Diagram Placeholder
  addImageFrame(slide, 7.0, 1.25, 5.5, 5.5, 'ARCHITECTURE DIAGRAM / DATA FLOW', 'Paste 3-Tier Layered Diagram or Postman API Test');
}

// ==========================================
// SLIDE 4: OOP Deep Dive (Split with Code Frame)
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addHeader(slide, 'Object-Oriented Programming (OOP) Deep Dive', 'Java OOP', 'Real-world implementation of the four OOP pillars in Java');

  // Left Side: 4 OOP Pillbox Cards
  const oopItems = [
    { title: 'Encapsulation', desc: 'Financial fields (balance, accNo) are private/protected. Mutations require synchronized methods using BigDecimal precision.', y: 1.25 },
    { title: 'Inheritance', desc: 'Person base class -> Customer (KYC/PAN) & Employee.\nAccount base -> SavingsAccount, CurrentAccount, FD.', y: 2.65 },
    { title: 'Polymorphism', desc: 'Overridden withdraw(): SavingsAccount checks min-balance, CurrentAccount supports overdraft, FD blocks early exit.', y: 4.05 },
    { title: 'Abstraction', desc: 'Abstract Account class prevents invalid instantiation while compelling concrete subtypes to define interest logic.', y: 5.45 }
  ];

  oopItems.forEach(o => {
    slide.addShape(pres.ShapeType.roundRect, {
      x: 0.8, y: o.y, w: 5.8, h: 1.25, rectRadius: 0.08,
      fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
    });
    slide.addText(o.title, {
      x: 1.0, y: o.y + 0.1, w: 5.4, h: 0.28, fontSize: 12, fontFace: 'Segoe UI', bold: true, color: C_BLUE
    });
    slide.addText(o.desc, {
      x: 1.0, y: o.y + 0.38, w: 5.4, h: 0.8, fontSize: 9.5, fontFace: 'Segoe UI', color: C_TEXT_DARK
    });
  });

  // Right Side: Model Code Screenshot Frame
  addImageFrame(slide, 7.0, 1.25, 5.5, 5.45, 'OOP CLASS HIERARCHY / MODEL CODE', 'Paste Screenshot of Account.java or Class Hierarchy Diagram');
}

// ==========================================
// SLIDE 5: Database Design & 3NF Normalization
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addHeader(slide, 'Database Design & 3NF Normalization', 'MySQL Schema', '12 Relational Tables in Third Normal Form in MySQL InnoDB');

  // Left Side: 3NF & Schema Cards
  slide.addShape(pres.ShapeType.roundRect, {
    x: 0.8, y: 1.25, w: 5.8, h: 5.5, rectRadius: 0.08,
    fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
  });
  slide.addText('12 Normalized Tables & Integrity Checks', {
    x: 1.0, y: 1.45, w: 5.4, h: 0.35, fontSize: 13, fontFace: 'Segoe UI', bold: true, color: C_NAVY
  });
  slide.addText([
    { text: '• Normalized Schema: ', options: { bold: true } },
    { text: 'branch, customer, employee, account_type, account, transaction, beneficiary, card, loan_type, loan, loan_payment, login.\n\n' },
    { text: '• 1NF (Atomicity): ', options: { bold: true } },
    { text: 'All columns store atomic values without repeating groups.\n\n' },
    { text: '• 2NF (Functional Dependency): ', options: { bold: true } },
    { text: 'No partial functional dependencies on composite keys.\n\n' },
    { text: '• 3NF (Zero Transitive Dependency): ', options: { bold: true } },
    { text: 'Branch IFSC and location isolated in branch master table. Eliminates update and deletion anomalies.\n\n' },
    { text: '• DB Constraints: ', options: { bold: true } },
    { text: 'CHECK (balance >= 0) and CHECK (status IN (\'ACTIVE\', \'FROZEN\', \'CLOSED\')).' }
  ], {
    x: 1.0, y: 1.85, w: 5.4, h: 4.7, fontSize: 10, fontFace: 'Segoe UI', color: C_TEXT_DARK
  });

  // Right Side: ER Diagram / MySQL Screenshot Frame
  addImageFrame(slide, 7.0, 1.25, 5.5, 5.5, 'MYSQL WORKBENCH ER DIAGRAM / SCHEMA', 'Paste Screenshot of MySQL Tables or SHOW TABLES output');
}

// ==========================================
// SLIDE 6: ACID Transactions & Concurrency Control
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addHeader(slide, 'ACID Transactions & Concurrency Control', 'Transactions', 'Ensuring atomic financial consistency during inter-account transfers');

  // Top Pipeline Card
  slide.addShape(pres.ShapeType.roundRect, {
    x: 0.8, y: 1.25, w: 11.7, h: 1.4, rectRadius: 0.08,
    fill: { color: 'EBF3FC' }, line: { color: C_ACCENT, width: 1.5 }
  });
  slide.addText('Transaction Pipeline (TransactionService.java):', {
    x: 1.0, y: 1.35, w: 11.3, h: 0.25, fontSize: 11, fontFace: 'Segoe UI', bold: true, color: C_BLUE
  });
  slide.addText('conn.setAutoCommit(false) -> 1. Row Lock (SELECT ... FOR UPDATE) -> 2. Debit Sender -> 3. Credit Receiver -> 4. Audit Log -> conn.commit()', {
    x: 1.0, y: 1.65, w: 11.3, h: 0.35, fontSize: 10, fontFace: 'Consolas', color: C_TEXT_DARK
  });
  slide.addText('On Exception / Error: conn.rollback() returns database to exact original state with zero funds lost.', {
    x: 1.0, y: 2.05, w: 11.3, h: 0.45, fontSize: 9.5, fontFace: 'Segoe UI', bold: true, color: C_RED
  });

  // Bottom Left: 4 ACID Cards in 2x2 grid
  const acidProps = [
    { title: 'Atomicity', desc: 'Debit and credit happen as one unit. Both succeed or both rollback.', x: 0.8, y: 2.85 },
    { title: 'Consistency', desc: 'System total balance invariants & non-negative checks are preserved.', x: 3.7, y: 2.85 },
    { title: 'Isolation', desc: 'InnoDB row locks prevent lost updates during concurrent transfers.', x: 0.8, y: 4.8 },
    { title: 'Durability', desc: 'Committed records written to MySQL Redo Log (WAL) on disk.', x: 3.7, y: 4.8 }
  ];

  acidProps.forEach(a => {
    slide.addShape(pres.ShapeType.roundRect, {
      x: a.x, y: a.y, w: 2.75, h: 1.85, rectRadius: 0.08,
      fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
    });
    slide.addText(a.title, {
      x: a.x + 0.15, y: a.y + 0.12, w: 2.45, h: 0.28, fontSize: 12, fontFace: 'Segoe UI', bold: true, color: C_NAVY
    });
    slide.addText(a.desc, {
      x: a.x + 0.15, y: a.y + 0.45, w: 2.45, h: 1.3, fontSize: 9.5, fontFace: 'Segoe UI', color: C_TEXT_DARK
    });
  });

  // Bottom Right: Code Screenshot Frame
  addImageFrame(slide, 6.7, 2.85, 5.8, 3.8, 'TRANSACTION CODE / LOG SCREENSHOT', 'Paste Screenshot of transferFunds() code or MySQL Terminal logs');
}

// ==========================================
// SLIDE 7: Frontend Architecture & UI Design
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addHeader(slide, 'Frontend Architecture & UI Design', 'Frontend', 'Clean, student-made, responsive vanilla web design with zero framework overhead');

  // Left Side: UI Highlights
  slide.addShape(pres.ShapeType.roundRect, {
    x: 0.8, y: 1.25, w: 5.8, h: 5.5, rectRadius: 0.08,
    fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
  });
  slide.addText('UI Design Philosophy & Features', {
    x: 1.0, y: 1.45, w: 5.4, h: 0.35, fontSize: 13, fontFace: 'Segoe UI', bold: true, color: C_NAVY
  });
  slide.addText([
    { text: '• Modern Design System: ', options: { bold: true } },
    { text: 'Deep Navy (#0B2545) primary, soft neutral surfaces, and clean card elevations with 8px rounded corners.\n\n' },
    { text: '• Zero Distracting Emojis: ', options: { bold: true } },
    { text: '100% clean, professional, academic labels throughout all pages.\n\n' },
    { text: '• Collapsible Dynamic Sidebar: ', options: { bold: true } },
    { text: 'Renders navigation matching user role with smooth 0.25s transitions. User preference (◂/▸) persisted in localStorage.\n\n' },
    { text: '• Client-Side Route Protection: ', options: { bold: true } },
    { text: 'checkAuth() guard checks session and strictly blocks Customers from Admin controls.\n\n' },
    { text: '• Asynchronous REST Integration: ', options: { bold: true } },
    { text: 'Native fetch() with async/await updates balance cards and tables seamlessly without page reloads.' }
  ], {
    x: 1.0, y: 1.85, w: 5.4, h: 4.7, fontSize: 9.8, fontFace: 'Segoe UI', color: C_TEXT_DARK
  });

  // Right Side: UI Screenshot Frame
  addImageFrame(slide, 7.0, 1.25, 5.5, 5.5, 'LOGIN & DASHBOARD UI SCREENSHOT', 'Paste Screenshot of login.html and dashboard.html');
}

// ==========================================
// SLIDE 8: Customer Banking Portal Features
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addHeader(slide, 'Customer Banking Portal Features', 'Customer Features', 'Comprehensive self-service banking operations for account holders');

  // Left Side: 4 Feature Cards
  const custFeats = [
    { title: 'Live Dashboard & Balance', desc: 'Account balance summary card with INR formatting, quick action shortcuts, and recent 5 transactions ledger.', y: 1.25 },
    { title: 'Deposit & Withdrawal', desc: 'Immediate crediting and debiting with instant validation preventing negative balance states.', y: 2.65 },
    { title: 'Instant Fund Transfer', desc: 'Atomic inter-account money transfer with automatic debit/credit balance reconciliation in MySQL.', y: 4.05 },
    { title: 'Sub-Accounts, Loans & KYC', desc: 'Open additional Savings/Current/FD accounts, apply for loans, and update verified KYC contact details.', y: 5.45 }
  ];

  custFeats.forEach(f => {
    slide.addShape(pres.ShapeType.roundRect, {
      x: 0.8, y: f.y, w: 5.8, h: 1.25, rectRadius: 0.08,
      fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
    });
    slide.addText(f.title, {
      x: 1.0, y: f.y + 0.1, w: 5.4, h: 0.28, fontSize: 12, fontFace: 'Segoe UI', bold: true, color: C_BLUE
    });
    slide.addText(f.desc, {
      x: 1.0, y: f.y + 0.38, w: 5.4, h: 0.8, fontSize: 9.5, fontFace: 'Segoe UI', color: C_TEXT_DARK
    });
  });

  // Right Side: Customer UI Screenshot Frame
  addImageFrame(slide, 7.0, 1.25, 5.5, 5.45, 'CUSTOMER PORTAL SCREENSHOT', 'Paste Screenshot of Customer Dashboard / Transfer Form');
}

// ==========================================
// SLIDE 9: Staff & Admin Management Panel
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addHeader(slide, 'Staff & Admin Management Panel', 'Staff Portal', 'Operational control center for bank staff and system administrators');

  // Left Side: 3 Feature Cards
  const staffFeats = [
    { title: 'Accounts Directory & Monitoring', desc: 'Real-time overview of all customer accounts across branches, showing live balances, account types, and operational statuses.', y: 1.25 },
    { title: 'Account Freeze / Activate Controls', desc: 'One-click security freeze on suspicious accounts. Frozen accounts are locked from withdrawals and transfers at API and DB layers.', y: 3.15 },
    { title: 'Loan Decision System', desc: 'Staff review pending loan applications (amount, category, tenure) and execute single-click Approve / Reject actions with real-time MySQL updates.', y: 5.05 }
  ];

  staffFeats.forEach(s => {
    slide.addShape(pres.ShapeType.roundRect, {
      x: 0.8, y: s.y, w: 5.8, h: 1.7, rectRadius: 0.08,
      fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
    });
    slide.addText(s.title, {
      x: 1.0, y: s.y + 0.15, w: 5.4, h: 0.35, fontSize: 12.5, fontFace: 'Segoe UI', bold: true, color: C_NAVY
    });
    slide.addText(s.desc, {
      x: 1.0, y: s.y + 0.55, w: 5.4, h: 1.05, fontSize: 10, fontFace: 'Segoe UI', color: C_TEXT_DARK
    });
  });

  // Right Side: Admin UI Screenshot Frame
  addImageFrame(slide, 7.0, 1.25, 5.5, 5.5, 'ADMIN PANEL & LOAN SCREENSHOT', 'Paste Screenshot of admin.html and loans.html');
}

// ==========================================
// SLIDE 10: Audit Logging & Transaction Filtering
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addHeader(slide, 'Audit Logging & Multi-Parameter Transaction History', 'Audit Trail', 'Immutable financial ledger auditing with server-side query filters');

  // Left Side: Audit Highlights
  slide.addShape(pres.ShapeType.roundRect, {
    x: 0.8, y: 1.25, w: 5.8, h: 5.5, rectRadius: 0.08,
    fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
  });
  slide.addText('Financial Compliance & Search Filters', {
    x: 1.0, y: 1.45, w: 5.4, h: 0.35, fontSize: 13, fontFace: 'Segoe UI', bold: true, color: C_NAVY
  });
  slide.addText([
    { text: '• Immutable Audit Ledger: ', options: { bold: true } },
    { text: 'Every financial movement (Deposit, Withdraw, Transfer) is logged with immutable timestamps and balance_after tracking. Financial records can never be overwritten.\n\n' },
    { text: '• Server-Side Date Filters: ', options: { bold: true } },
    { text: 'Users and auditors can filter transactions between specific start and end dates (From Date / To Date). Backend safely binds dates in parameterized SQL queries.\n\n' },
    { text: '• Transaction Type Filtering: ', options: { bold: true } },
    { text: 'Instant classification by Credit (+) or Debit (-). Color-coded status badges allow rapid visual verification of cash movements.' }
  ], {
    x: 1.0, y: 1.9, w: 5.4, h: 4.6, fontSize: 10, fontFace: 'Segoe UI', color: C_TEXT_DARK
  });

  // Right Side: Transaction Table Screenshot Frame
  addImageFrame(slide, 7.0, 1.25, 5.5, 5.5, 'TRANSACTION HISTORY TABLE SCREENSHOT', 'Paste Screenshot of transactions.html with Date/Type filters applied');
}

// ==========================================
// SLIDE 11: Testing & Automated Verification
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addHeader(slide, 'System Testing & Automated Verification', 'Verification', 'Comprehensive 22-step End-to-End and API test suite validating stability');

  // Left Side: Stat Badge Card
  slide.addShape(pres.ShapeType.roundRect, {
    x: 0.8, y: 1.25, w: 5.8, h: 5.5, rectRadius: 0.08,
    fill: { color: C_GREEN_BG }, line: { color: C_GREEN, width: 2 }
  });
  slide.addText('AUTOMATED SUITE RESULTS', {
    x: 1.0, y: 1.5, w: 5.4, h: 0.3, fontSize: 12, fontFace: 'Segoe UI', bold: true, color: C_GREEN, align: 'center'
  });
  slide.addText('22 / 22', {
    x: 1.0, y: 1.9, w: 5.4, h: 0.9, fontSize: 44, fontFace: 'Segoe UI', bold: true, color: C_GREEN, align: 'center'
  });
  slide.addText('TESTS PASSED (100% SUCCESS)\nZero Failures Reported', {
    x: 1.0, y: 2.85, w: 5.4, h: 0.55, fontSize: 12, fontFace: 'Segoe UI', bold: true, color: C_GREEN, align: 'center'
  });
  slide.addText([
    { text: 'Validated Test Coverage:\n', options: { bold: true } },
    { text: '• Authentication & Session Validation (HTTP 200/401)\n' },
    { text: '• Deposit/Withdraw Mathematical Precision in MySQL\n' },
    { text: '• Atomic Transfer Consistency (Sender & Receiver)\n' },
    { text: '• Overdraft and Insufficient Funds Error Handling\n' },
    { text: '• Bad Input Sanitization (Non-numeric / Negative amounts)\n' },
    { text: '• Filtered Query Validation (Date range & Type filters)' }
  ], {
    x: 1.2, y: 3.6, w: 5.0, h: 2.9, fontSize: 9.5, fontFace: 'Segoe UI', color: C_TEXT_DARK
  });

  // Right Side: Terminal Output Screenshot Frame
  addImageFrame(slide, 7.0, 1.25, 5.5, 5.5, 'TERMINAL TEST RUNNER SCREENSHOT', 'Paste Screenshot of SystemTest.java output showing 22 [PASS] rows');
}

// ==========================================
// SLIDE 12: Technology Stack & Tools
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addHeader(slide, 'Technology Stack & Development Environment', 'Tech Stack', 'Standard, high-performance tools with zero unnecessary third-party overhead');

  const stackCols = [
    { title: 'Backend Layer', items: '• Java 11+\n• Core OOP Models\n• Embedded HttpServer\n• Connector/J 8.3.0\n• Port 8080 REST API', color: C_NAVY, x: 0.8 },
    { title: 'Database Layer', items: '• MySQL 8.0 Server\n• InnoDB Engine\n• 12 Tables in 3NF\n• ACID Row Locks\n• Foreign Key Rules', color: C_BLUE, x: 3.8 },
    { title: 'Frontend Layer', items: '• Semantic HTML5\n• CSS3 Variables\n• Vanilla ES6 JS\n• fetch() REST Client\n• localStorage Session', color: C_ACCENT, x: 6.8 },
    { title: 'Tools & DevOps', items: '• VS Code & Terminal\n• Git & GitHub\n• MySQL Workbench\n• Batch Scripts (.bat)\n• SystemTest Runner', color: '3F51B5', x: 9.8 }
  ];

  stackCols.forEach(s => {
    slide.addShape(pres.ShapeType.roundRect, {
      x: s.x, y: 1.25, w: 2.75, h: 5.5, rectRadius: 0.08,
      fill: { color: C_CARD_BG }, line: { color: s.color, width: 2 }
    });
    slide.addShape(pres.ShapeType.rect, {
      x: s.x, y: 1.25, w: 2.75, h: 0.55, fill: { color: s.color }
    });
    slide.addText(s.title, {
      x: s.x + 0.1, y: 1.35, w: 2.55, h: 0.35, fontSize: 12, fontFace: 'Segoe UI', bold: true, color: C_WHITE, align: 'center'
    });
    slide.addText(s.items, {
      x: s.x + 0.2, y: 2.0, w: 2.35, h: 4.5, fontSize: 10.5, fontFace: 'Segoe UI', color: C_TEXT_DARK, lineSpacingMultiple: 1.25
    });
  });
}

// ==========================================
// SLIDE 13: Conclusion & Thank You (Dark Ending Theme)
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_NAVY };

  slide.addText('Project Conclusion & Live Demo', {
    x: 0.8, y: 0.8, w: 11.7, h: 0.6,
    fontSize: 26, fontFace: 'Segoe UI', bold: true, color: C_WHITE, align: 'center'
  });

  // Summary Card
  slide.addShape(pres.ShapeType.roundRect, {
    x: 1.2, y: 1.6, w: 10.9, h: 2.4, rectRadius: 0.1,
    fill: { color: C_NAVY_LIGHT }, line: { color: '244B78', width: 1 }
  });
  slide.addText([
    { text: 'Key Achievements & Takeaways:\n', options: { bold: true, color: C_WHITE } },
    { text: '• Built a robust, production-grade 3-tier banking system from scratch without bulky frameworks.\n', options: { color: 'D8E6F5' } },
    { text: '• Successfully bridged Java OOP domain models with a 3NF normalized MySQL database.\n', options: { color: 'D8E6F5' } },
    { text: '• Enforced strict ACID transactional integrity on all inter-account financial operations.\n', options: { color: 'D8E6F5' } },
    { text: '• Verified 100% test pass rate across 22 automated integration tests in SystemTest.java.', options: { color: 'D8E6F5' } }
  ], {
    x: 1.5, y: 1.8, w: 10.3, h: 2.0,
    fontSize: 11.5, fontFace: 'Segoe UI', lineSpacingMultiple: 1.25
  });

  slide.addText('Thank You!', {
    x: 0.8, y: 4.3, w: 11.7, h: 0.8,
    fontSize: 34, fontFace: 'Segoe UI', bold: true, color: 'FFFFFF', align: 'center'
  });

  slide.addText('We are now ready for Questions & Live System Demonstration', {
    x: 0.8, y: 5.15, w: 11.7, h: 0.45,
    fontSize: 15, fontFace: 'Segoe UI', color: 'A0B8D8', align: 'center'
  });

  slide.addText('Project Repository: github.com/patilmanasvi/Bank-Management-System', {
    x: 0.8, y: 5.75, w: 11.7, h: 0.35,
    fontSize: 11.5, fontFace: 'Consolas', color: 'C0D4EC', align: 'center'
  });
}

// Generate the PPTX File
pres.writeFile({ fileName: 'Bank_Management_System_Presentation.pptx' })
  .then(fileName => {
    console.log(`SUCCESS: Cleaned and resized PPTX saved as ${fileName}`);
  })
  .catch(err => {
    console.error('ERROR generating presentation:', err);
  });
