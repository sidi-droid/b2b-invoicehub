# B2B InvoiceHub — Demo Credentials

Use these accounts to showcase the full role-based flow. All client accounts are
pre-approved — they will auto-seed into the browser on first load of the app.

## Admin

| Role  | Password   |
|-------|------------|
| Admin | `admin123` |

Sign in: Login page → click **Admin** tile → enter password → Continue.

## Clients (pre-approved demo accounts)

Clients sign in with their **personal User ID** (no more dropdown — privacy-preserving).

| Company                       | User ID          | Password      | Credit Limit |
|-------------------------------|------------------|---------------|--------------|
| Sharma Enterprises Pvt Ltd    | `sharma_ent`     | `sharma123`   | ₹5,00,000    |
| MegaTech Solutions            | `megatech`       | `mega123`     | ₹3,00,000    |
| Global Traders Co.            | `global_traders` | `global123`   | ₹7,50,000    |
| Sunrise Distributors          | `sunrise`        | `sunrise123`  | ₹2,00,000    |
| Horizon Retail Ltd            | `horizon`        | `horizon123`  | ₹4,00,000    |
| Drewrk Labs pvt ltd           | `drewrk`         | `drewrk123`   | ₹75,000      |

Sign in: Login page → click **Client** tile → enter User ID + password → Continue.

## New client registration (live demo)

From the login page (Client tile) → **Register as a new client** link → fill form →
submit. The request appears on the **Registrations** tab in the admin sidebar
(with a red pending-count badge). Admin clicks **Approve** to create the client
in the backend and activate the login.

## Notes

- Client auth is stored in browser `localStorage` under key `b2b-client-accounts`.
- Demo seeding runs once on first load (tracked by flag `b2b-demo-seeded-v1`).
- To wipe and re-seed:
  ```js
  localStorage.removeItem('b2b-client-accounts');
  localStorage.removeItem('b2b-demo-seeded-v1');
  location.reload();
  ```
- Sessions are stored in `sessionStorage` (tab-scoped), so closing the tab logs you out but refresh keeps you in.
