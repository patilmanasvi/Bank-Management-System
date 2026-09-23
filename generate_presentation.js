const pptxgen = require('pptxgenjs');

const pres = new pptxgen();
pres.layout = 'LAYOUT_16x9';
pres.author = 'Bank Management System Team';
pres.company = 'Computer Engineering Dept';
pres.title = 'Bank Management System - Project Presentation';

// Colors
const C_NAVY = '0B2545';
const C_BLUE = '1D4E89';
const C_ACCENT = '2A6FC9';
const C_BG_LIGHT = 'F7F8FA';
const C_CARD_BG = 'FFFFFF';
const C_BORDER = 'DDE2E8';
const C_TEXT_DARK = '0B2545';
const C_TEXT_MUTED = '5B6B7E';
const C_WHITE = 'FFFFFF';
const C_GREEN = '1F7A4D';
const C_GREEN_BG = 'E6F4EA';
const C_RED = 'B3261E';

// Helper: Add Standard Header
function addSlideHeader(slide, title, subtitle) {
  // Top header banner background
  slide.addShape(pres.ShapeType.rect, {
    x: 0, y: 0, w: '100%', h: 1.1,
    fill: { color: C_NAVY }
  });
  slide.addText(title, {
    x: 0.8, y: 0.15, w: 11.5, h: 0.5,
    fontSize: 22, fontFace: 'Segoe UI', bold: true, color: C_WHITE
  });
  if (subtitle) {
    slide.addText(subtitle, {
      x: 0.8, y: 0.65, w: 11.5, h: 0.35,
      fontSize: 12, fontFace: 'Segoe UI', color: 'A0B8D8'
    });
  }
}

// ==========================================
// SLIDE 1: Title Slide
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_NAVY };

  // Decorative header line
  slide.addShape(pres.ShapeType.rect, {
    x: 1.2, y: 1.5, w: 1.2, h: 0.08,
    fill: { color: C_ACCENT }
  });

  slide.addText('Bank Management System', {
    x: 1.2, y: 1.8, w: 10.5, h: 1.1,
    fontSize: 38, fontFace: 'Segoe UI', bold: true, color: C_WHITE
  });

  slide.addText('An Enterprise 3-Tier Banking System Demonstrating Advanced OOP, 3NF Relational DBMS & ACID Transactions', {
    x: 1.2, y: 3.0, w: 10.5, h: 0.8,
    fontSize: 16, fontFace: 'Segoe UI', color: 'C0D4EC'
  });

  // Metadata Card
  slide.addShape(pres.ShapeType.roundRect, {
    x: 1.2, y: 4.2, w: 10.8, h: 2.2, rectRadius: 0.15,
    fill: { color: '13345A' }, line: { color: '244B78', width: 1 }
  });

  slide.addText([
    { text: 'Course / Subject: ', options: { bold: true, color: C_WHITE } },
    { text: 'OOP in Java & Database Management Systems (DBMS)\n', options: { color: 'D8E6F5' } },
    { text: 'Architecture: ', options: { bold: true, color: C_WHITE } },
    { text: 'Java Backend (REST Server) | MySQL InnoDB (12 Tables) | Vanilla JS Frontend\n', options: { color: 'D8E6F5' } },
    { text: 'Project Domain: ', options: { bold: true, color: C_WHITE } },
    { text: 'Retail Banking, ACID Fund Transfers, KYC Management & Loan Approvals', options: { color: 'D8E6F5' } }
  ], {
    x: 1.5, y: 4.4, w: 10.2, h: 1.8,
    fontSize: 13, fontFace: 'Segoe UI', lineSpacingMultiple: 1.2
  });
}

// ==========================================
// SLIDE 2: Problem Statement & Objectives
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addSlideHeader(slide, 'Problem Statement & Project Objectives', 'Addressing core architectural bottlenecks in traditional banking implementations');

  // Left Card: Challenges
  slide.addShape(pres.ShapeType.roundRect, {
    x: 0.8, y: 1.5, w: 5.4, h: 5.2, rectRadius: 0.1,
    fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
  });
  slide.addText('Challenges in Traditional Software', {
    x: 1.1, y: 1.7, w: 4.8, h: 0.4,
    fontSize: 16, fontFace: 'Segoe UI', bold: true, color: C_RED
  });
  slide.addText([
    { text: 'High UI-Database Coupling:\n', options: { bold: true } },
    { text: 'Direct SQL in UI files causes fragility and security vulnerabilities.\n\n' },
    { text: 'Concurrency Anomalies:\n', options: { bold: true } },
    { text: 'Without ACID transaction boundaries, simultaneous transfers cause dirty reads and lost updates.\n\n' },
    { text: 'Unnormalized Data Schemas:\n', options: { bold: true } },
    { text: 'Redundant branch and customer data leads to update and deletion anomalies.' }
  ], {
    x: 1.1, y: 2.2, w: 4.8, h: 4.2,
    fontSize: 12, fontFace: 'Segoe UI', color: C_TEXT_DARK
  });

  // Right Card: Objectives
  slide.addShape(pres.ShapeType.roundRect, {
    x: 6.8, y: 1.5, w: 5.6, h: 5.2, rectRadius: 0.1,
    fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
  });
  slide.addText('Our Core Project Objectives', {
    x: 7.1, y: 1.7, w: 5.0, h: 0.4,
    fontSize: 16, fontFace: 'Segoe UI', bold: true, color: C_GREEN
  });
  slide.addText([
    { text: 'Decoupled 3-Tier Layering:\n', options: { bold: true } },
    { text: 'Clear separation of Presentation, Business Logic (Java), and Database (MySQL).\n\n' },
    { text: 'Guaranteed ACID Compliance:\n', options: { bold: true } },
    { text: 'Atomic all-or-nothing fund transfers using database transaction management.\n\n' },
    { text: 'Strict 3NF Schema Normalization:\n', options: { bold: true } },
    { text: '12 normalized tables eliminating transitive dependencies and redundant records.\n\n' },
    { text: 'Role-Based Access Control (RBAC):\n', options: { bold: true } },
    { text: 'Fine-grained privilege separation for Customers, Staff, and Administrators.' }
  ], {
    x: 7.1, y: 2.2, w: 5.0, h: 4.2,
    fontSize: 12, fontFace: 'Segoe UI', color: C_TEXT_DARK
  });
}

// ==========================================
// SLIDE 3: 3-Tier System Architecture
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addSlideHeader(slide, '3-Tier System Architecture', 'Decoupled data flow from browser presentation to MySQL InnoDB storage');

  const layers = [
    { title: '1. Presentation Layer (Frontend & Desktop)', desc: 'HTML5, CSS3, ES6 JavaScript Web Portal | Java Swing Desktop GUI\nCommunicates purely via asynchronous REST JSON requests (fetch API).', color: C_BLUE, y: 1.5 },
    { title: '2. Application Layer (Java Backend & Services)', desc: 'Embedded Java HTTP Server (Port 8080) | REST API Endpoints\nOOP Models (Account, Customer) & Services (TransactionService, LoanService, AccountService)', color: C_ACCENT, y: 3.3 },
    { title: '3. Database Layer (MySQL 8.0 Engine)', desc: 'MySQL InnoDB Relational Database with 12 Tables in 3NF Normalization\nEnforces Foreign Keys, CHECK constraints, Row-Level Locks, and ACID Transactions', color: C_NAVY, y: 5.1 }
  ];

  layers.forEach(layer => {
    slide.addShape(pres.ShapeType.roundRect, {
      x: 1.0, y: layer.y, w: 11.2, h: 1.5, rectRadius: 0.1,
      fill: { color: C_CARD_BG }, line: { color: layer.color, width: 2 }
    });
    slide.addShape(pres.ShapeType.rect, {
      x: 1.0, y: layer.y, w: 0.25, h: 1.5,
      fill: { color: layer.color }
    });
    slide.addText(layer.title, {
      x: 1.4, y: layer.y + 0.15, w: 10.5, h: 0.4,
      fontSize: 15, fontFace: 'Segoe UI', bold: true, color: layer.color
    });
    slide.addText(layer.desc, {
      x: 1.4, y: layer.y + 0.55, w: 10.5, h: 0.8,
      fontSize: 12, fontFace: 'Segoe UI', color: C_TEXT_DARK
    });
  });
}

// ==========================================
// SLIDE 4: OOP Deep Dive
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addSlideHeader(slide, 'Object-Oriented Programming (OOP) Deep Dive', 'Real-world implementation of the 4 core pillars in Java');

  const oopPillars = [
    {
      title: 'Encapsulation',
      desc: 'All sensitive financial fields (balance, accountNumber) are protected/private.\nDirect balance edits are blocked. All balance modifications require synchronized domain methods with positive-amount validation using BigDecimal.',
      x: 0.8, y: 1.5
    },
    {
      title: 'Inheritance',
      desc: 'Entity Hierarchy:\nPerson base class extended by Customer (adds KYC/PAN/DOB) and Employee (adds designation/salary).\nAccount Hierarchy:\nAccount base extended by SavingsAccount, CurrentAccount, FixedDepositAccount.',
      x: 6.8, y: 1.5
    },
    {
      title: 'Polymorphism',
      desc: 'Method Overriding:\n• SavingsAccount: Enforces minimum balance threshold.\n• CurrentAccount: Overdraft support allows negative balance up to limit.\n• FixedDepositAccount: Blocks withdrawals before maturity.\n• Overridden calculateInterest() logic.',
      x: 0.8, y: 4.1
    },
    {
      title: 'Abstraction',
      desc: 'Abstract Class Account:\nCannot be instantiated directly (new Account() is prohibited).\nEnforces common balance state while compelling concrete subclasses to supply their unique interest and withdrawal behaviors.',
      x: 6.8, y: 4.1
    }
  ];

  oopPillars.forEach(p => {
    slide.addShape(pres.ShapeType.roundRect, {
      x: p.x, y: p.y, w: 5.6, h: 2.5, rectRadius: 0.1,
      fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
    });
    slide.addText(p.title, {
      x: p.x + 0.3, y: p.y + 0.15, w: 5.0, h: 0.35,
      fontSize: 15, fontFace: 'Segoe UI', bold: true, color: C_BLUE
    });
    slide.addText(p.desc, {
      x: p.x + 0.3, y: p.y + 0.55, w: 5.0, h: 1.8,
      fontSize: 11.5, fontFace: 'Segoe UI', color: C_TEXT_DARK
    });
  });
}

// ==========================================
// SLIDE 5: Database Design & 3NF Normalization
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addSlideHeader(slide, 'Database Design & 3NF Schema Normalization', '12 Relational Tables in Third Normal Form in MySQL InnoDB');

  // Left Card: Schema breakdown
  slide.addShape(pres.ShapeType.roundRect, {
    x: 0.8, y: 1.5, w: 5.5, h: 5.2, rectRadius: 0.1,
    fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
  });
  slide.addText('12 Normalized Relational Tables', {
    x: 1.1, y: 1.7, w: 5.0, h: 0.35,
    fontSize: 15, fontFace: 'Segoe UI', bold: true, color: C_NAVY
  });
  slide.addText([
    { text: '1. branch: ', options: { bold: true } }, { text: 'Branch locations & IFSC codes\n' },
    { text: '2. customer: ', options: { bold: true } }, { text: 'Customer KYC, PAN & contact data\n' },
    { text: '3. employee: ', options: { bold: true } }, { text: 'Bank personnel & designations\n' },
    { text: '4. account_type: ', options: { bold: true } }, { text: 'Account categories & base rates\n' },
    { text: '5. account: ', options: { bold: true } }, { text: 'Master ledger with live balances\n' },
    { text: '6. transaction: ', options: { bold: true } }, { text: 'Immutable debit/credit audit trail\n' },
    { text: '7. loan_type / loan: ', options: { bold: true } }, { text: 'Loan categories & applications\n' },
    { text: '8. login: ', options: { bold: true } }, { text: 'Role-based credentials (Admin/Staff/User)' }
  ], {
    x: 1.1, y: 2.1, w: 5.0, h: 4.4,
    fontSize: 11.5, fontFace: 'Segoe UI', color: C_TEXT_DARK, lineSpacingMultiple: 1.15
  });

  // Right Card: Normalization proof
  slide.addShape(pres.ShapeType.roundRect, {
    x: 6.7, y: 1.5, w: 5.7, h: 5.2, rectRadius: 0.1,
    fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
  });
  slide.addText('3NF Normalization & Integrity Guarantees', {
    x: 7.0, y: 1.7, w: 5.2, h: 0.35,
    fontSize: 15, fontFace: 'Segoe UI', bold: true, color: C_BLUE
  });
  slide.addText([
    { text: '1NF (Atomicity):\n', options: { bold: true } },
    { text: 'All table columns hold atomic values; no multi-valued attributes.\n\n' },
    { text: '2NF (No Partial Key Dependency):\n', options: { bold: true } },
    { text: 'Non-key attributes are fully dependent on table primary keys.\n\n' },
    { text: '3NF (No Transitive Dependencies):\n', options: { bold: true } },
    { text: 'Branch IFSC and addresses isolated in branch master table. Eliminates update and deletion anomalies.\n\n' },
    { text: 'Engine Constraints:\n', options: { bold: true } },
    { text: 'CHECK (balance >= 0) and CHECK (status IN (\'ACTIVE\', \'FROZEN\', \'CLOSED\')) enforced at database level.' }
  ], {
    x: 7.0, y: 2.1, w: 5.2, h: 4.4,
    fontSize: 11.5, fontFace: 'Segoe UI', color: C_TEXT_DARK
  });
}

// ==========================================
// SLIDE 6: ACID Transactions & Concurrency
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addSlideHeader(slide, 'ACID Transactions & Concurrency Control', 'Ensuring atomic financial consistency during inter-account transfers');

  // Flowchart Pipeline
  slide.addShape(pres.ShapeType.roundRect, {
    x: 0.8, y: 1.5, w: 11.6, h: 2.2, rectRadius: 0.1,
    fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
  });
  slide.addText('Transaction Execution Flow in TransactionService.java:', {
    x: 1.1, y: 1.65, w: 11.0, h: 0.3,
    fontSize: 13, fontFace: 'Segoe UI', bold: true, color: C_BLUE
  });
  slide.addText('1. conn.setAutoCommit(false) -> 2. Row Lock & Balance Validation -> 3. Debit Sender -> 4. Credit Receiver -> 5. Write Audit Log -> 6. conn.commit()', {
    x: 1.1, y: 2.05, w: 11.0, h: 0.4,
    fontSize: 12, fontFace: 'Consolas', color: C_TEXT_DARK
  });
  slide.addText('Exception / Failure State -> conn.rollback() guarantees zero partial balances or lost funds.', {
    x: 1.1, y: 2.5, w: 11.0, h: 0.3,
    fontSize: 12, fontFace: 'Segoe UI', bold: true, color: C_RED
  });

  // ACID Breakdown (4 cards)
  const acidCards = [
    { title: 'Atomicity', desc: 'Debit from sender and credit to receiver happen as one single indivisible unit. Both succeed or both rollback.', x: 0.8 },
    { title: 'Consistency', desc: 'Balance non-negative constraints and system total balance invariants remain preserved before and after execution.', x: 3.8 },
    { title: 'Isolation', desc: 'MySQL InnoDB row-level locking (SELECT ... FOR UPDATE) prevents dirty reads and lost updates during concurrent operations.', x: 6.8 },
    { title: 'Durability', desc: 'Once conn.commit() completes, changes are written to MySQL Redo Log (WAL) and permanently saved on physical disk.', x: 9.8 }
  ];

  acidCards.forEach(c => {
    slide.addShape(pres.ShapeType.roundRect, {
      x: c.x, y: 3.9, w: 2.6, h: 2.8, rectRadius: 0.1,
      fill: { color: C_CARD_BG }, line: { color: C_ACCENT, width: 1.5 }
    });
    slide.addText(c.title, {
      x: c.x + 0.2, y: 4.1, w: 2.2, h: 0.35,
      fontSize: 14, fontFace: 'Segoe UI', bold: true, color: C_NAVY
    });
    slide.addText(c.desc, {
      x: c.x + 0.2, y: 4.5, w: 2.2, h: 2.0,
      fontSize: 11, fontFace: 'Segoe UI', color: C_TEXT_DARK
    });
  });
}

// ==========================================
// SLIDE 7: Frontend Architecture & UI Design
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addSlideHeader(slide, 'Frontend Architecture & User Experience', 'Clean, student-made, and responsive vanilla web design with zero heavy frameworks');

  const uiFeatures = [
    {
      title: 'Design System & Visual Theme',
      desc: '• Palette: Deep Navy (#0B2545), Pure White Cards (#FFFFFF), Soft Border (#DDE2E8).\n• Softened 8px rounded corners across all cards, inputs, and tables.\n• 100% clean, professional, distraction-free labels (Zero emojis).\n• Modern Segoe UI typography with clear information hierarchy.',
      x: 0.8, y: 1.5
    },
    {
      title: 'Dynamic Role-Aware Sidebar',
      desc: '• Dynamically renders navigation links matching logged-in user role.\n• Collapsible Sidebar with smooth 0.25s CSS transitions.\n• User toggle preference (◂/▸) persisted in localStorage across page loads.\n• Responsive layout adapts main-content margin automatically.',
      x: 6.8, y: 1.5
    },
    {
      title: 'Client-Side Route Protection',
      desc: '• checkAuth() guard checks localStorage session on every page load.\n• Unauthenticated access immediately redirected to login.\n• Customers strictly blocked from accessing Admin Control Panel.\n• Centralized logout clears session and redirects.',
      x: 0.8, y: 4.1
    },
    {
      title: 'Asynchronous REST Integration',
      desc: '• Built with modern ES6 async/await and native fetch() API.\n• Seamless DOM updates (balance cards, status badges, tables) without page reloads.\n• User feedback via clean success and error alert banners.\n• Zero external library dependencies (No React/Bootstrap overhead).',
      x: 6.8, y: 4.1
    }
  ];

  uiFeatures.forEach(f => {
    slide.addShape(pres.ShapeType.roundRect, {
      x: f.x, y: f.y, w: 5.6, h: 2.5, rectRadius: 0.1,
      fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
    });
    slide.addText(f.title, {
      x: f.x + 0.3, y: f.y + 0.15, w: 5.0, h: 0.35,
      fontSize: 14, fontFace: 'Segoe UI', bold: true, color: C_BLUE
    });
    slide.addText(f.desc, {
      x: f.x + 0.3, y: f.y + 0.55, w: 5.0, h: 1.8,
      fontSize: 11, fontFace: 'Segoe UI', color: C_TEXT_DARK
    });
  });
}

// ==========================================
// SLIDE 8: Customer Banking Portal Features
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addSlideHeader(slide, 'Customer Banking Portal Features', 'End-to-end self-service banking operations for account holders');

  const customerFeats = [
    { title: 'Live Dashboard', desc: 'Account balance summary card with INR formatting, quick action buttons, and recent 5 transactions ledger.', x: 0.8, y: 1.5 },
    { title: 'Deposit & Withdrawal', desc: 'Real-time crediting and debiting with instant validation preventing negative balance states.', x: 4.8, y: 1.5 },
    { title: 'Atomic Fund Transfer', desc: 'Instant inter-account money transfer with automatic debit/credit balance reconciliation in MySQL.', x: 8.8, y: 1.5 },
    { title: 'Open New Account', desc: 'Self-service creation of additional Savings (4%), Current (Overdraft), or Fixed Deposit (7.5%) accounts.', x: 0.8, y: 4.1 },
    { title: 'Loan Applications', desc: 'Apply for Home, Personal, Car, or Education loans with customizable tenure and interest calculation.', x: 4.8, y: 4.1 },
    { title: 'KYC & Profile Settings', desc: 'Inspect official verified PAN/DOB records, update contact info (Phone, Email, Address), and change password.', x: 8.8, y: 4.1 }
  ];

  customerFeats.forEach(c => {
    slide.addShape(pres.ShapeType.roundRect, {
      x: c.x, y: c.y, w: 3.6, h: 2.5, rectRadius: 0.1,
      fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
    });
    slide.addText(c.title, {
      x: c.x + 0.2, y: c.y + 0.2, w: 3.2, h: 0.35,
      fontSize: 14, fontFace: 'Segoe UI', bold: true, color: C_NAVY
    });
    slide.addText(c.desc, {
      x: c.x + 0.2, y: c.y + 0.6, w: 3.2, h: 1.7,
      fontSize: 11.5, fontFace: 'Segoe UI', color: C_TEXT_DARK
    });
  });
}

// ==========================================
// SLIDE 9: Staff & Admin Management Panel
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addSlideHeader(slide, 'Staff & Admin Management Panel', 'Operational control center for bank employees and system managers');

  const adminCards = [
    {
      title: 'Customer Accounts Directory',
      desc: '• Real-time inspection of all customer accounts in the bank.\n• Live balance monitoring across all branches.\n• Color-coded operational status badges (ACTIVE / FROZEN).\n• Immediate search and table refresh controls.',
      x: 0.8
    },
    {
      title: 'Account Freeze / Activate Controls',
      desc: '• One-click security freeze on suspicious or compromised accounts.\n• Frozen accounts are immediately locked out from withdrawals and transfers at API and DB layers.\n• One-click reactivation restores normal operations.',
      x: 4.8
    },
    {
      title: 'Loan Approval & Decision System',
      desc: '• Bank staff review pending customer loan applications.\n• Inspects applicant name, requested amount, category, and tenure.\n• Interactive Approve / Reject buttons with immediate MySQL status updates.',
      x: 8.8
    }
  ];

  adminCards.forEach(c => {
    slide.addShape(pres.ShapeType.roundRect, {
      x: c.x, y: 1.6, w: 3.6, h: 5.0, rectRadius: 0.1,
      fill: { color: C_CARD_BG }, line: { color: C_BLUE, width: 1.5 }
    });
    slide.addText(c.title, {
      x: c.x + 0.2, y: 1.8, w: 3.2, h: 0.5,
      fontSize: 15, fontFace: 'Segoe UI', bold: true, color: C_NAVY
    });
    slide.addText(c.desc, {
      x: c.x + 0.2, y: 2.4, w: 3.2, h: 4.0,
      fontSize: 12, fontFace: 'Segoe UI', color: C_TEXT_DARK, lineSpacingMultiple: 1.2
    });
  });
}

// ==========================================
// SLIDE 10: Audit Logging & Transaction Filtering
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addSlideHeader(slide, 'Audit Logging & Multi-Parameter Transaction History', 'Immutable financial ledger auditing with server-side query filters');

  slide.addShape(pres.ShapeType.roundRect, {
    x: 0.8, y: 1.5, w: 11.6, h: 2.2, rectRadius: 0.1,
    fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
  });
  slide.addText('Immutable Financial Audit Trail', {
    x: 1.1, y: 1.7, w: 11.0, h: 0.35,
    fontSize: 15, fontFace: 'Segoe UI', bold: true, color: C_NAVY
  });
  slide.addText('Every financial event (Deposit, Withdrawal, Transfer) is recorded into the MySQL transaction table with immutable timestamps, before/after balance tracking, and transaction classification. Financial records can never be overwritten or tampered with.', {
    x: 1.1, y: 2.1, w: 11.0, h: 1.4,
    fontSize: 12, fontFace: 'Segoe UI', color: C_TEXT_DARK
  });

  // Filter Cards
  slide.addShape(pres.ShapeType.roundRect, {
    x: 0.8, y: 4.0, w: 5.6, h: 2.7, rectRadius: 0.1,
    fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
  });
  slide.addText('Date Range Search Filters', {
    x: 1.1, y: 4.2, w: 5.0, h: 0.35,
    fontSize: 14, fontFace: 'Segoe UI', bold: true, color: C_BLUE
  });
  slide.addText('Users and auditors can filter transactions between specific start and end dates (From Date / To Date). Backend constructs parameterized SQL date queries safely without injection risk.', {
    x: 1.1, y: 4.6, w: 5.0, h: 1.9,
    fontSize: 11.5, fontFace: 'Segoe UI', color: C_TEXT_DARK
  });

  slide.addShape(pres.ShapeType.roundRect, {
    x: 6.8, y: 4.0, w: 5.6, h: 2.7, rectRadius: 0.1,
    fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
  });
  slide.addText('Transaction Type Classification', {
    x: 7.1, y: 4.2, w: 5.0, h: 0.35,
    fontSize: 14, fontFace: 'Segoe UI', bold: true, color: C_BLUE
  });
  slide.addText('Instant categorization by Credit (+) or Debit (-). Color-coded badges and green/red monospace amount formatting allow quick audit verification of all cash movements.', {
    x: 7.1, y: 4.6, w: 5.0, h: 1.9,
    fontSize: 11.5, fontFace: 'Segoe UI', color: C_TEXT_DARK
  });
}

// ==========================================
// SLIDE 11: Testing & Verification
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addSlideHeader(slide, 'System Testing & Automated Verification', 'Comprehensive 22-step End-to-End and API test suite validating system stability');

  // Big Green Badge Card
  slide.addShape(pres.ShapeType.roundRect, {
    x: 0.8, y: 1.5, w: 4.0, h: 5.2, rectRadius: 0.1,
    fill: { color: C_GREEN_BG }, line: { color: C_GREEN, width: 2 }
  });
  slide.addText('Test Results', {
    x: 1.0, y: 1.8, w: 3.6, h: 0.4,
    fontSize: 16, fontFace: 'Segoe UI', bold: true, color: C_GREEN, align: 'center'
  });
  slide.addText('22 / 22', {
    x: 1.0, y: 2.4, w: 3.6, h: 1.0,
    fontSize: 42, fontFace: 'Segoe UI', bold: true, color: C_GREEN, align: 'center'
  });
  slide.addText('PASSED (100% SUCCESS)\nZero Failures Reported', {
    x: 1.0, y: 3.6, w: 3.6, h: 0.8,
    fontSize: 13, fontFace: 'Segoe UI', bold: true, color: C_GREEN, align: 'center'
  });
  slide.addText('Automated suite built using Java native HttpClient in SystemTest.java.', {
    x: 1.0, y: 4.8, w: 3.6, h: 1.5,
    fontSize: 11, fontFace: 'Segoe UI', color: C_TEXT_DARK, align: 'center'
  });

  // Right Card: Tested Areas
  slide.addShape(pres.ShapeType.roundRect, {
    x: 5.2, y: 1.5, w: 7.2, h: 5.2, rectRadius: 0.1,
    fill: { color: C_CARD_BG }, line: { color: C_BORDER, width: 1 }
  });
  slide.addText('Validated Test Categories', {
    x: 5.5, y: 1.7, w: 6.6, h: 0.35,
    fontSize: 15, fontFace: 'Segoe UI', bold: true, color: C_NAVY
  });
  slide.addText([
    { text: '• Authentication & Sessions: ', options: { bold: true } }, { text: 'Valid/invalid credential validation with HTTP 200/401.\n\n' },
    { text: '• Deposit & Withdraw Accuracy: ', options: { bold: true } }, { text: 'Mathematical balance verification in MySQL.\n\n' },
    { text: '• Atomic Fund Transfers: ', options: { bold: true } }, { text: 'Simultaneous sender debit & receiver credit consistency.\n\n' },
    { text: '• Boundary & Overdraft Checks: ', options: { bold: true } }, { text: 'Negative balance prevention & error messages.\n\n' },
    { text: '• Bad Input Sanitization: ', options: { bold: true } }, { text: 'Non-numeric, negative, and missing payloads rejected with clean HTTP 400 (no server crashes).\n\n' },
    { text: '• Filtered Query Validation: ', options: { bold: true } }, { text: 'Type-specific and date-specific history queries.' }
  ], {
    x: 5.5, y: 2.1, w: 6.6, h: 4.4,
    fontSize: 11.5, fontFace: 'Segoe UI', color: C_TEXT_DARK
  });
}

// ==========================================
// SLIDE 12: Technology Stack
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_BG_LIGHT };
  addSlideHeader(slide, 'Technology Stack & Development Tools', 'Standard, high-performance tools with zero unnecessary third-party overhead');

  const techCards = [
    { title: 'Backend Layer', items: '• Java 11+\n• Core OOP & Concurrency\n• Embedded HttpServer\n• MySQL Connector/J 8.3.0', color: C_NAVY, x: 0.8 },
    { title: 'Database Layer', items: '• MySQL 8.0 Server\n• InnoDB Storage Engine\n• 12 Normalized 3NF Tables\n• ACID Transaction Locks', color: C_BLUE, x: 3.8 },
    { title: 'Frontend Layer', items: '• HTML5 Semantic Layout\n• CSS3 (Flex, Grid, Variables)\n• Vanilla ES6 JavaScript\n• Native fetch() REST Client', color: C_ACCENT, x: 6.8 },
    { title: 'Tools & Workflow', items: '• VS Code & Terminal\n• Git & GitHub Version Control\n• MySQL Workbench\n• Batch (.bat) Automation', color: '3F51B5', x: 9.8 }
  ];

  techCards.forEach(t => {
    slide.addShape(pres.ShapeType.roundRect, {
      x: t.x, y: 1.6, w: 2.6, h: 5.0, rectRadius: 0.1,
      fill: { color: C_CARD_BG }, line: { color: t.color, width: 2 }
    });
    slide.addShape(pres.ShapeType.rect, {
      x: t.x, y: 1.6, w: 2.6, h: 0.7,
      fill: { color: t.color }
    });
    slide.addText(t.title, {
      x: t.x + 0.1, y: 1.75, w: 2.4, h: 0.4,
      fontSize: 13, fontFace: 'Segoe UI', bold: true, color: C_WHITE, align: 'center'
    });
    slide.addText(t.items, {
      x: t.x + 0.2, y: 2.6, w: 2.2, h: 3.8,
      fontSize: 11.5, fontFace: 'Segoe UI', color: C_TEXT_DARK, lineSpacingMultiple: 1.2
    });
  });
}

// ==========================================
// SLIDE 13: Conclusion & Thank You
// ==========================================
{
  const slide = pres.addSlide();
  slide.background = { color: C_NAVY };

  slide.addText('Project Conclusion & Live Demo', {
    x: 1.0, y: 1.0, w: 11.2, h: 0.6,
    fontSize: 28, fontFace: 'Segoe UI', bold: true, color: C_WHITE, align: 'center'
  });

  // Summary Card
  slide.addShape(pres.ShapeType.roundRect, {
    x: 1.5, y: 1.8, w: 10.2, h: 2.4, rectRadius: 0.1,
    fill: { color: '13345A' }, line: { color: '244B78', width: 1 }
  });
  slide.addText([
    { text: 'Key Achievements & Takeaways:\n', options: { bold: true, color: C_WHITE } },
    { text: '• Built a fully functional, enterprise-grade 3-tier banking system from scratch.\n', options: { color: 'D8E6F5' } },
    { text: '• Successfully bridged OOP domain models with a 3NF normalized relational MySQL schema.\n', options: { color: 'D8E6F5' } },
    { text: '• Enforced strict ACID transactional integrity preventing real-world concurrency flaws.\n', options: { color: 'D8E6F5' } },
    { text: '• Verified 100% test passing rate across 22 automated end-to-end integration tests.', options: { color: 'D8E6F5' } }
  ], {
    x: 1.8, y: 2.0, w: 9.6, h: 2.0,
    fontSize: 12, fontFace: 'Segoe UI', lineSpacingMultiple: 1.2
  });

  slide.addText('Thank You!', {
    x: 1.0, y: 4.5, w: 11.2, h: 0.8,
    fontSize: 36, fontFace: 'Segoe UI', bold: true, color: 'FFFFFF', align: 'center'
  });

  slide.addText('We are now ready for Questions & Live Demonstration', {
    x: 1.0, y: 5.4, w: 11.2, h: 0.5,
    fontSize: 16, fontFace: 'Segoe UI', color: 'A0B8D8', align: 'center'
  });

  slide.addText('GitHub: github.com/patilmanasvi/Bank-Management-System', {
    x: 1.0, y: 6.0, w: 11.2, h: 0.4,
    fontSize: 12, fontFace: 'Consolas', color: 'C0D4EC', align: 'center'
  });
}

// Generate the PPTX File
pres.writeFile({ fileName: 'Bank_Management_System_Presentation.pptx' })
  .then(fileName => {
    console.log(`SUCCESS: PowerPoint Presentation saved as ${fileName}`);
  })
  .catch(err => {
    console.error('ERROR generating presentation:', err);
  });
