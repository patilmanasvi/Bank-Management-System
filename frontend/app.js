// Bank Management System - Frontend API Connector (Student 3 Integration)

const API_BASE = window.location.origin;

// Get current session
function getSession() {
  const user = localStorage.getItem('bank_user');
  return user ? JSON.parse(user) : { username: 'aarav', accountNumber: 'ACC100101', role: 'CUSTOMER', balance: 80000 };
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
        alert(`Login Successful! Welcome, ${data.customerName || data.username} (${data.role})`);
        window.location.href = 'dashboard.html';
      } else {
        alert('Login Failed: ' + (data.message || 'Invalid credentials'));
      }
    } catch (err) {
      alert('Error connecting to server: ' + err.message);
    }
  });
}

// 2. Handle Dashboard
async function setupDashboard() {
  const balanceEl = document.getElementById('dashboardBalance');
  const userEl = document.getElementById('dashboardUser');
  const session = getSession();

  if (userEl) userEl.innerText = `Welcome, ${session.customerName || session.username} | `;

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

  // Load recent transactions
  const tbody = document.querySelector('#recentTransactions tbody');
  if (tbody) {
    try {
      const res = await fetch(`${API_BASE}/api/transactions/history?acc=${session.accountNumber}`);
      if (res.ok) {
        const list = await res.json();
        tbody.innerHTML = '';
        list.slice(0, 5).forEach(t => {
          const isCredit = t.type === 'DEPOSIT' || (t.type === 'TRANSFER' && t.target === session.accountNumber);
          const tr = document.createElement('tr');
          tr.innerHTML = `
            <td>${t.date.substring(0, 10)}</td>
            <td>${t.desc}</td>
            <td class="amount" style="color:${isCredit ? 'green' : 'red'};">${isCredit ? '+' : '-'} ₹ ${parseFloat(t.amount).toFixed(2)}</td>
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
        alert('Deposit Successful!');
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
        alert('Withdrawal Successful!');
        window.location.href = 'dashboard.html';
      } else {
        alert('Withdrawal Failed: ' + data.message);
      }
    } catch (err) {
      alert('Error: ' + err.message);
    }
  });
}

// 5. Handle Transfer
function setupTransferForm() {
  const form = document.getElementById('transferForm');
  if (!form) return;
  const session = getSession();

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const toAccount = document.getElementById('toAccount').value.trim();
    const amount = document.getElementById('amount').value.trim();
    const remarks = document.getElementById('description') ? document.getElementById('description').value : 'Transfer';

    try {
      const res = await fetch(`${API_BASE}/api/transactions/transfer`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ fromAccount: session.accountNumber, toAccount, amount, remarks })
      });
      const data = await res.json();
      if (data.success) {
        alert('Atomic Transfer Completed Successfully in MySQL!');
        window.location.href = 'dashboard.html';
      } else {
        alert('Transfer Failed: ' + data.message);
      }
    } catch (err) {
      alert('Error: ' + err.message);
    }
  });
}

// 6. Handle All Transactions
async function setupTransactionsPage() {
  const tbody = document.querySelector('#transactionsTable tbody');
  if (!tbody) return;
  const session = getSession();

  try {
    const res = await fetch(`${API_BASE}/api/transactions/history?acc=${session.accountNumber}`);
    if (res.ok) {
      const list = await res.json();
      tbody.innerHTML = '';
      list.forEach(t => {
        const isCredit = t.type === 'DEPOSIT';
        const tr = document.createElement('tr');
        tr.innerHTML = `
          <td>${t.date}</td>
          <td>${t.desc}</td>
          <td><span class="badge ${t.type.toLowerCase()}">${t.type}</span></td>
          <td class="amount" style="color:${isCredit ? 'green' : 'red'};">${isCredit ? '+' : '-'} ₹ ${parseFloat(t.amount).toFixed(2)}</td>
          <td>₹ ${parseFloat(t.balance).toFixed(2)}</td>
        `;
        tbody.appendChild(tr);
      });
    }
  } catch (e) {
    console.error(e);
  }
}

// Auto init based on page
document.addEventListener('DOMContentLoaded', () => {
  setupLoginForm();
  setupDashboard();
  setupDepositForm();
  setupWithdrawForm();
  setupTransferForm();
  setupTransactionsPage();
});
