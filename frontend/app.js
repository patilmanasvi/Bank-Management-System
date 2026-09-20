// Bank Management System - Frontend Controller & API Connector

const API_BASE = window.location.origin;

// Retrieve session from localStorage
function getSession() {
  const user = localStorage.getItem('bank_user');
  return user ? JSON.parse(user) : null;
}

// Security Check: Redirect to login if user is not authenticated
function checkAuth() {
  const currentPage = window.location.pathname.split('/').pop() || 'index.html';
  if (currentPage === 'login.html' || currentPage === 'signup.html' || currentPage === 'index.html' || currentPage === '') {
    return;
  }
  const session = getSession();
  if (!session) {
    alert('Please sign in to access your bank account.');
    window.location.href = 'login.html';
    return;
  }

  // Role Access Control: Customers cannot access admin panel
  if (currentPage === 'admin.html' && session.role === 'CUSTOMER') {
    alert('Access Denied: Admin Panel is restricted to Bank Staff and Managers.');
    window.location.href = 'dashboard.html';
    return;
  }
}

// Render dynamic role-based sidebar navigation
function renderSidebar() {
  const session = getSession();
  const sidebar = document.getElementById('mainSidebar') || document.querySelector('nav.sidebar');
  if (!sidebar || !session) return;

  const current = window.location.pathname.split('/').pop() || 'dashboard.html';
  const isStaff = session.role === 'ADMIN' || session.role === 'EMPLOYEE';

  let links = [];

  if (isStaff) {
    // Staff / Employee / Admin Navigation
    links = [
      { href: 'dashboard.html', text: 'Overview Dashboard' },
      { href: 'admin.html', text: 'Accounts & Freeze/Activate' },
      { href: 'loans.html', text: 'Loan Approvals' },
      { href: 'deposit.html', text: 'Counter Deposit' },
      { href: 'withdraw.html', text: 'Counter Withdrawal' },
      { href: 'transactions.html', text: 'Audit Transactions' },
      { href: 'profile.html', text: 'My Staff Profile' }
    ];
  } else {
    // Customer Navigation
    links = [
      { href: 'dashboard.html', text: 'My Dashboard' },
      { href: 'deposit.html', text: 'Deposit Funds' },
      { href: 'withdraw.html', text: 'Withdraw Funds' },
      { href: 'transfer.html', text: 'Transfer Money' },
      { href: 'transactions.html', text: 'Transaction History' },
      { href: 'open_account.html', text: 'Open New Account' },
      { href: 'loans.html', text: 'Apply for Loan' },
      { href: 'profile.html', text: 'My Profile & KYC' }
    ];
  }

  sidebar.innerHTML = '';
  links.forEach(item => {
    const a = document.createElement('a');
    a.href = item.href;
    a.innerText = item.text;
    if (current === item.href) a.className = 'active';
    sidebar.appendChild(a);
  });
}

// Sidebar toggle (collapse/expand)
function initSidebarToggle() {
  const sidebar = document.getElementById('mainSidebar') || document.querySelector('nav.sidebar');
  const mainContent = document.querySelector('.main-content');
  if (!sidebar || !mainContent) return;

  // Create toggle button
  const btn = document.createElement('button');
  btn.className = 'sidebar-toggle';
  btn.title = 'Toggle Sidebar';
  btn.innerHTML = '&#9666;'; // left arrow
  document.body.appendChild(btn);

  // Restore saved state
  const saved = localStorage.getItem('sidebar_collapsed');
  if (saved === 'true') {
    sidebar.classList.add('collapsed');
    mainContent.classList.add('expanded');
    btn.classList.add('shifted');
    btn.innerHTML = '&#9656;'; // right arrow
  }

  btn.addEventListener('click', () => {
    const isCollapsed = sidebar.classList.toggle('collapsed');
    mainContent.classList.toggle('expanded');
    btn.classList.toggle('shifted');
    btn.innerHTML = isCollapsed ? '&#9656;' : '&#9666;';
    localStorage.setItem('sidebar_collapsed', isCollapsed);
  });
}

// Update header info and role badge
function updateHeader() {
  const session = getSession();
  if (!session) return;

  const userEl = document.getElementById('headerUser') || document.getElementById('dashboardUser');
  const badgeEl = document.getElementById('headerRoleBadge');

  if (userEl) {
    userEl.innerHTML = `Signed in: <strong>${session.customerName || session.username}</strong> | `;
  }

  if (badgeEl) {
    badgeEl.innerText = session.role;
    badgeEl.className = `badge ${session.role === 'CUSTOMER' ? 'active' : 'pending'}`;
  }

  const logoutBtn = document.getElementById('btnLogout') || document.querySelector('header a[href="login.html"]');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', (e) => {
      e.preventDefault();
      localStorage.removeItem('bank_user');
      window.location.href = 'login.html';
    });
  }
}

// 1. Handle Login
function setupLoginForm() {
  const form = document.getElementById('loginForm');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value.trim();

    try {
      const res = await fetch(`${API_BASE}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });
      const data = await res.json();
      if (data.success) {
        localStorage.setItem('bank_user', JSON.stringify(data));
        alert(`Login Successful!\nWelcome, ${data.customerName || data.username} (${data.role})`);
        window.location.href = (data.role === 'ADMIN' || data.role === 'EMPLOYEE') ? 'admin.html' : 'dashboard.html';
      } else {
        alert('Login Failed: ' + (data.message || 'Invalid email or password'));
      }
    } catch (err) {
      alert('Error connecting to server: ' + err.message);
    }
  });
}

// 2. Handle Dashboard
async function setupDashboard() {
  const balanceEl = document.getElementById('dashboardBalance');
  const welcomeBanner = document.getElementById('dashboardBanner');
  const session = getSession();
  if (!session) return;

  if (welcomeBanner) {
    if (session.role === 'CUSTOMER') {
      welcomeBanner.innerHTML = `
        <div style="background:#e8f4fd; border:1px solid #b6d4fe; padding:12px 16px; border-radius:6px; margin-bottom:20px; font-size:14px; color:#084298;">
          <strong>Hello, ${session.customerName || session.username}!</strong> You are signed in as a <strong>Customer</strong>. 
          Your primary account is <code>${session.accountNumber}</code>. You can deposit money, make atomic fund transfers, check your transaction history, or apply for loans below.
        </div>`;
    } else {
      welcomeBanner.innerHTML = `
        <div style="background:#fff3cd; border:1px solid #ffe69c; padding:12px 16px; border-radius:6px; margin-bottom:20px; font-size:14px; color:#664d03;">
          <strong>Welcome, ${session.customerName || session.username}!</strong> You are signed in with <strong>${session.role}</strong> staff privileges. 
          You can inspect all customer accounts in the <strong>Admin Panel</strong>, freeze or activate accounts, and evaluate pending loan applications.
        </div>`;
    }
  }

  // Fetch live balance
  try {
    const res = await fetch(`${API_BASE}/api/account/details?acc=${session.accountNumber}`);
    if (res.ok) {
      const data = await res.json();
      if (balanceEl) balanceEl.innerText = `₹ ${parseFloat(data.balance).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
      session.balance = data.balance;
      localStorage.setItem('bank_user', JSON.stringify(session));
    }
  } catch (e) {
    console.warn('Using cached balance');
  }

  // Fetch recent transactions
  const tbody = document.querySelector('#recentTransactions tbody');
  if (tbody) {
    try {
      const res = await fetch(`${API_BASE}/api/transactions/history?acc=${session.accountNumber}`);
      if (res.ok) {
        const list = await res.json();
        tbody.innerHTML = '';
        if (list.length === 0) {
          tbody.innerHTML = '<tr><td colspan="4" style="text-align:center; color:#666;">No recent transactions yet. Make a deposit or transfer to get started!</td></tr>';
          return;
        }
        list.slice(0, 5).forEach(t => {
          const isCredit = t.type === 'DEPOSIT' || (t.type === 'TRANSFER' && t.target === session.accountNumber);
          const tr = document.createElement('tr');
          tr.innerHTML = `
            <td>${t.date ? t.date.substring(0, 10) : ''}</td>
            <td>${t.desc || ''}</td>
            <td class="amount" style="color:${isCredit ? '#137333' : '#c5221f'}; font-weight:bold;">${isCredit ? '+' : '-'} ₹ ${parseFloat(t.amount).toFixed(2)}</td>
            <td>₹ ${parseFloat(t.balance).toFixed(2)}</td>
          `;
          tbody.appendChild(tr);
        });
      }
    } catch (e) {
      console.error(e);
    }
  }
}

// 3. Handle Deposit
function setupDepositForm() {
  const form = document.getElementById('depositForm');
  if (!form) return;
  const session = getSession();

  const accField = document.getElementById('account');
  if (accField && session) accField.value = session.accountNumber;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const amount = document.getElementById('amount').value;
    try {
      const res = await fetch(`${API_BASE}/api/transactions/deposit`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ accountNumber: session.accountNumber, amount })
      });
      const data = await res.json();
      if (data.success) {
        alert('₹ ' + parseFloat(amount).toFixed(2) + ' Deposited Successfully!\nAccount balance updated in database.');
        window.location.href = 'dashboard.html';
      } else {
        alert('Deposit Failed: ' + data.message);
      }
    } catch (err) {
      alert('Error: ' + err.message);
    }
  });
}

// 4. Handle Withdraw
function setupWithdrawForm() {
  const form = document.getElementById('withdrawForm');
  if (!form) return;
  const session = getSession();

  const accField = document.getElementById('account');
  if (accField && session) accField.value = session.accountNumber;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const amount = document.getElementById('amount').value;
    try {
      const res = await fetch(`${API_BASE}/api/transactions/withdraw`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ accountNumber: session.accountNumber, amount })
      });
      const data = await res.json();
      if (data.success) {
        alert('₹ ' + parseFloat(amount).toFixed(2) + ' Withdrawn Successfully!\nAccount balance updated in database.');
        window.location.href = 'dashboard.html';
      } else {
        alert('Withdrawal Failed: ' + data.message);
      }
    } catch (err) {
      alert('Error: ' + err.message);
    }
  });
}

// 5. Handle Transfer (ACID)
function setupTransferForm() {
  const form = document.getElementById('transferForm');
  if (!form) return;
  const session = getSession();

  const fromField = document.getElementById('from');
  if (fromField && session) fromField.value = session.accountNumber;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const toAccount = document.getElementById('toAccount').value.trim();
    const amount = document.getElementById('amount').value.trim();
    const remarks = document.getElementById('description') ? document.getElementById('description').value : 'Fund Transfer';

    if (toAccount === session.accountNumber) {
      alert('Validation Error: You cannot transfer funds to your own source account.');
      return;
    }

    try {
      const res = await fetch(`${API_BASE}/api/transactions/transfer`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ fromAccount: session.accountNumber, toAccount, amount, remarks })
      });
      const data = await res.json();
      if (data.success) {
        alert(`Transfer Successful!\n₹ ${amount} transferred from ${session.accountNumber} to ${toAccount}.\nAtomic debit and credit recorded in MySQL.`);
        window.location.href = 'dashboard.html';
      } else {
        alert('Transfer Failed (Transaction Rolled Back): ' + data.message);
      }
    } catch (err) {
      alert('Error: ' + err.message);
    }
  });
}

// 6. Handle Transactions History with Server-Side Filters
async function setupTransactionsPage() {
  const tbody = document.querySelector('#transactionsTable tbody');
  const filterForm = document.getElementById('filterForm');
  if (!tbody) return;
  const session = getSession();

  async function loadFilteredHistory(type, from, to) {
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">Loading records from database...</td></tr>';
    let url = `${API_BASE}/api/transactions/history?acc=${session.accountNumber}`;
    if (type && type !== 'all') url += `&type=${encodeURIComponent(type.toUpperCase())}`;
    if (from) url += `&from=${encodeURIComponent(from)}`;
    if (to) url += `&to=${encodeURIComponent(to)}`;

    try {
      const res = await fetch(url);
      if (res.ok) {
        const list = await res.json();
        tbody.innerHTML = '';
        if (list.length === 0) {
          tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:#666;">No transaction records match the criteria.</td></tr>';
          return;
        }
        list.forEach(t => {
          const isCredit = t.type === 'DEPOSIT';
          const tr = document.createElement('tr');
          tr.innerHTML = `
            <td>${t.date || ''}</td>
            <td>${t.desc || ''}</td>
            <td><span class="badge ${t.type.toLowerCase()}">${t.type}</span></td>
            <td class="amount" style="color:${isCredit ? '#137333' : '#c5221f'}; font-weight:bold;">${isCredit ? '+' : '-'} ₹ ${parseFloat(t.amount).toFixed(2)}</td>
            <td>₹ ${parseFloat(t.balance).toFixed(2)}</td>
          `;
          tbody.appendChild(tr);
        });
      }
    } catch (e) {
      console.error(e);
      tbody.innerHTML = '<tr><td colspan="5" class="error">Error loading transactions.</td></tr>';
    }
  }

  if (filterForm) {
    filterForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const type = document.getElementById('type').value;
      const from = document.getElementById('startDate').value;
      const to = document.getElementById('endDate').value;
      loadFilteredHistory(type, from, to);
    });
  }

  loadFilteredHistory('all', null, null);
}

// 7. Handle Admin / Employee Panel
async function setupAdminPage() {
  const tbody = document.querySelector('#adminAccountsTable tbody');
  const refreshBtn = document.getElementById('btnRefreshAccounts');
  if (!tbody) return;

  async function loadAccounts() {
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Fetching accounts from database...</td></tr>';
    try {
      const res = await fetch(`${API_BASE}/api/admin/accounts`);
      if (res.ok) {
        const accounts = await res.json();
        tbody.innerHTML = '';
        accounts.forEach(acc => {
          const isFrozen = acc.status === 'FROZEN';
          const tr = document.createElement('tr');
          tr.innerHTML = `
            <td><strong>${acc.accountNumber}</strong></td>
            <td>${acc.customerName}</td>
            <td>${acc.accountType}</td>
            <td class="amount" style="font-weight:bold;">₹ ${parseFloat(acc.balance).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
            <td><span class="badge ${acc.status.toLowerCase()}">${acc.status}</span></td>
            <td>
              ${isFrozen 
                ? `<button class="success-btn small" onclick="changeAccountStatus('${acc.accountNumber}', 'activate')">Activate</button>`
                : `<button class="danger small" onclick="changeAccountStatus('${acc.accountNumber}', 'freeze')">Freeze</button>`
              }
            </td>
          `;
          tbody.appendChild(tr);
        });
      }
    } catch (e) {
      tbody.innerHTML = '<tr><td colspan="6" class="error">Error loading accounts from database.</td></tr>';
    }
  }

  if (refreshBtn) refreshBtn.addEventListener('click', loadAccounts);
  loadAccounts();
}

// Helper: Toggle account status (Freeze / Activate)
window.changeAccountStatus = async function(accountNumber, action) {
  const confirmMsg = action === 'freeze' 
    ? `Freeze account ${accountNumber}? This will block deposits, withdrawals, and outward transfers.` 
    : `Reactivate account ${accountNumber}? Full transaction privileges will be restored.`;
  if (!confirm(confirmMsg)) return;

  try {
    const res = await fetch(`${API_BASE}/api/admin/account/${action}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ accountNumber })
    });
    const data = await res.json();
    if (data.success) {
      alert(`Account ${accountNumber} has been ${action === 'freeze' ? 'FROZEN' : 'ACTIVATED'} successfully!`);
      setupAdminPage();
    } else {
      alert('Operation failed: ' + data.message);
    }
  } catch (err) {
    alert('Error: ' + err.message);
  }
};

// 8. Handle Loan Management
async function setupLoansPage() {
  const applyForm = document.getElementById('loanApplyForm');
  const tbody = document.querySelector('#loansTable tbody');
  const refreshBtn = document.getElementById('btnRefreshLoans');
  const session = getSession();

  // Apply Form
  if (applyForm) {
    applyForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const loanTypeId = parseInt(document.getElementById('loanType').value);
      const amount = document.getElementById('loanAmount').value;
      const tenureMonths = parseInt(document.getElementById('loanTenure').value);
      const branchId = parseInt(document.getElementById('loanBranch').value);
      const msgEl = document.getElementById('loanApplyMsg');

      try {
        const res = await fetch(`${API_BASE}/api/loan/apply`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            customerId: session ? session.customerId || 1001 : 1001,
            loanTypeId,
            branchId,
            amount,
            tenureMonths
          })
        });
        const data = await res.json();
        if (data.success) {
          msgEl.className = 'success';
          msgEl.innerText = `Loan Application Submitted! Application ID: ${data.loanId}. Status: PENDING review by bank staff.`;
          msgEl.style.display = 'block';
          applyForm.reset();
          loadLoans();
        } else {
          msgEl.className = 'error';
          msgEl.innerText = 'Application Error: ' + data.message;
          msgEl.style.display = 'block';
        }
      } catch (err) {
        alert('Error: ' + err.message);
      }
    });
  }

  // Load Loans List
  async function loadLoans() {
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;">Loading loan records from database...</td></tr>';
    try {
      const res = await fetch(`${API_BASE}/api/loan/list`);
      if (res.ok) {
        const loans = await res.json();
        tbody.innerHTML = '';
        if (loans.length === 0) {
          tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; color:#666;">No loan records found.</td></tr>';
          return;
        }
        const isStaff = session && (session.role === 'ADMIN' || session.role === 'EMPLOYEE');
        loans.forEach(l => {
          const isPending = l.status === 'PENDING';
          const tr = document.createElement('tr');
          tr.innerHTML = `
            <td>#${l.loanId}</td>
            <td>${l.customerName || 'Customer #' + l.customerId}</td>
            <td>${l.loanType}</td>
            <td class="amount">₹ ${parseFloat(l.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
            <td>${l.interestRate}%</td>
            <td>${l.tenureMonths} mos</td>
            <td><span class="badge ${l.status.toLowerCase()}">${l.status}</span></td>
            <td>
              ${isPending && isStaff
                ? `<button class="success-btn small" onclick="decideLoan(${l.loanId}, 'APPROVED')">Approve</button>
                   <button class="danger small" onclick="decideLoan(${l.loanId}, 'REJECTED')">Reject</button>`
                : `<span style="color:#666; font-size:12px;">${isPending ? 'Under Review' : 'Processed'}</span>`
              }
            </td>
          `;
          tbody.appendChild(tr);
        });
      }
    } catch (e) {
      tbody.innerHTML = '<tr><td colspan="8" class="error">Error loading loans from database.</td></tr>';
    }
  }

  if (refreshBtn) refreshBtn.addEventListener('click', loadLoans);
  loadLoans();
}

// Staff decide loan (Approve / Reject)
window.decideLoan = async function(loanId, decision) {
  if (!confirm(`Are you sure you want to set loan #${loanId} to ${decision}?`)) return;

  try {
    const res = await fetch(`${API_BASE}/api/loan/decision`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ loanId, decision, empId: 101 })
    });
    const data = await res.json();
    if (data.success) {
      alert(`Loan #${loanId} has been marked as ${decision}!`);
      setupLoansPage();
    } else {
      alert('Error updating loan: ' + data.message);
    }
  } catch (err) {
    alert('Error: ' + err.message);
  }
};

// 9. Handle Open New Account
function setupOpenAccountForm() {
  const form = document.getElementById('openAccountForm');
  const resultEl = document.getElementById('openAccResult');
  if (!form) return;
  const session = getSession();

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const accountType = document.getElementById('accType').value;
    const initialDeposit = document.getElementById('initialDeposit').value;
    const branchId = parseInt(document.getElementById('accBranch').value);

    try {
      const res = await fetch(`${API_BASE}/api/account/open`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          customerId: session ? session.customerId || 1001 : 1001,
          branchId,
          accountType,
          initialDeposit
        })
      });
      const data = await res.json();
      if (data.success) {
        resultEl.style.display = 'block';
        resultEl.style.backgroundColor = '#e6f4ea';
        resultEl.style.color = '#137333';
        resultEl.innerHTML = `
          <strong>Account Provisioned Successfully!</strong><br>
          Assigned Account Number: <strong>${data.accountNumber}</strong><br>
          Account Type: <strong>${accountType}</strong> | Opening Balance: <strong>₹ ${parseFloat(initialDeposit).toFixed(2)}</strong><br>
          <p style="margin-top:8px;"><a href="dashboard.html" style="color:#137333; font-weight:bold;">Go to Dashboard</a></p>
        `;
        form.reset();
      } else {
        resultEl.style.display = 'block';
        resultEl.style.backgroundColor = '#fce8e6';
        resultEl.style.color = '#c5221f';
        resultEl.innerHTML = `<strong>Account Opening Failed:</strong> ${data.message}`;
      }
    } catch (err) {
      alert('Error: ' + err.message);
    }
  });
}

// 10. Handle User Profile & Edit Settings
async function setupProfilePage() {
  const form = document.getElementById('profileForm');
  const changePwdForm = document.getElementById('changePasswordForm');
  if (!form) return;
  const session = getSession();
  if (!session) return;

  const profMsg = document.getElementById('profMsg');
  const pwdMsg = document.getElementById('pwdMsg');
  const accountsTbody = document.querySelector('#profAccountsTable tbody');

  // Load profile from server
  try {
    const res = await fetch(`${API_BASE}/api/user/profile?user=${encodeURIComponent(session.username || session.email)}`);
    if (res.ok) {
      const p = await res.json();
      document.getElementById('profName').value = p.name || '';
      document.getElementById('profRole').value = p.role || '';
      document.getElementById('profBranch').value = p.branch || '';

      const kycBox = document.getElementById('customerKycFields');
      if (p.role === 'CUSTOMER') {
        if (kycBox) kycBox.style.display = 'block';
        document.getElementById('profPan').value = p.pan || '';
        document.getElementById('profDob').value = p.dob || '';
      } else {
        if (kycBox) kycBox.style.display = 'none';
      }

      document.getElementById('profPhone').value = p.phone || '';
      document.getElementById('profEmail').value = p.email || '';
      document.getElementById('profAddress').value = p.address || '';

      // Populate accounts table
      if (accountsTbody && p.accounts) {
        accountsTbody.innerHTML = '';
        p.accounts.forEach(acc => {
          const tr = document.createElement('tr');
          tr.innerHTML = `
            <td><strong>${acc.accountNumber}</strong></td>
            <td>${acc.type}</td>
            <td class="amount">₹ ${parseFloat(acc.balance).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
            <td><span class="badge ${acc.status.toLowerCase()}">${acc.status}</span></td>
          `;
          accountsTbody.appendChild(tr);
        });
      }
    }
  } catch (err) {
    console.error('Error fetching profile:', err);
  }

  // Handle Profile Update Submission
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const phone = document.getElementById('profPhone').value.trim();
    const email = document.getElementById('profEmail').value.trim();
    const address = document.getElementById('profAddress').value.trim();

    try {
      const res = await fetch(`${API_BASE}/api/user/profile/update`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: session.username,
          phone,
          email,
          address
        })
      });
      const data = await res.json();
      if (data.success) {
        profMsg.className = 'success';
        profMsg.innerText = 'Profile details updated successfully in MySQL database!';
        profMsg.style.display = 'block';
      } else {
        profMsg.className = 'error';
        profMsg.innerText = 'Update failed: ' + data.message;
        profMsg.style.display = 'block';
      }
    } catch (err) {
      alert('Error updating profile: ' + err.message);
    }
  });

  // Handle Change Password Submission
  if (changePwdForm) {
    changePwdForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const oldPassword = document.getElementById('oldPassword').value.trim();
      const newPassword = document.getElementById('newPassword').value.trim();
      const confirmPassword = document.getElementById('confirmPassword').value.trim();

      if (newPassword !== confirmPassword) {
        pwdMsg.className = 'error';
        pwdMsg.innerText = 'New password and confirm password do not match!';
        pwdMsg.style.display = 'block';
        return;
      }

      try {
        const res = await fetch(`${API_BASE}/api/user/change-password`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            username: session.username,
            oldPassword,
            newPassword
          })
        });
        const data = await res.json();
        if (data.success) {
          pwdMsg.className = 'success';
          pwdMsg.innerText = 'Password changed successfully in MySQL database!';
          pwdMsg.style.display = 'block';
          changePwdForm.reset();
        } else {
          pwdMsg.className = 'error';
          pwdMsg.innerText = 'Error: ' + data.message;
          pwdMsg.style.display = 'block';
        }
      } catch (err) {
        alert('Error changing password: ' + err.message);
      }
    });
  }
}

// Auto init on page load
document.addEventListener('DOMContentLoaded', () => {
  checkAuth();
  renderSidebar();
  initSidebarToggle();
  updateHeader();
  setupLoginForm();
  setupDashboard();
  setupDepositForm();
  setupWithdrawForm();
  setupTransferForm();
  setupTransactionsPage();
  setupAdminPage();
  setupLoansPage();
  setupOpenAccountForm();
  setupProfilePage();
});
