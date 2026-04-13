# Expensio 💰
### A desktop expense tracker built with Java Swing

Expensio is a clean, dark-themed desktop app for tracking your personal expenses. Add expenses, set budgets, view visual reports, and keep your spending under control — all stored locally on your machine.

---

## Features

### Dashboard
- At-a-glance summary cards — total spent, this month's spending, top category, and entry count
- Recent expenses list (last 6 entries)
- Live pie chart showing spending breakdown by category

### Expenses
- Add, edit, and delete expense entries
- Each entry stores: amount, category, description, and date (auto-recorded)
- Filter by category or month
- Color-coded category badges

### Budgets
- Set a spending limit for any category
- Visual progress bar shows how much of each budget you've used
- Color changes from green → amber → red as you approach or exceed the limit
- Live warnings when a new expense pushes you over budget
- "Set Budget" shortcut from any unbudgeted category card

### Reports
- Grand total, average monthly spend, and biggest single expense
- Monthly bar chart (last 6 months)
- Top 8 highest expenses list

### Dark / Light mode
- Toggle between dark and light theme from the sidebar — applies instantly across the whole app

### Persistent storage
- All data is auto-saved to CSV when you close the app
- Auto-loaded on next launch — no setup required
- Manual "Save Now" button in the sidebar for peace of mind

---

## Project Structure

```
src/
└── com/expenseTracker/
    ├── Main.java              Entry point — launches the Swing GUI on the EDT
    ├── Expense.java           Data model: id, amount, category, date, description
    ├── ExpenseManager.java    Core logic: add, delete, edit, filter, budgets, totals
    ├── FileHandler.java       CSV save & load for expenses and budgets
    ├── AppTheme.java          Design system: colors, fonts, factory methods for UI components
    ├── MainWindow.java        Root JFrame: sidebar nav + CardLayout content area
    ├── DashboardPanel.java    Dashboard screen with stat cards, recent list, pie chart
    ├── ExpensePanel.java      Expense table with add/edit/delete dialogs and filters
    ├── BudgetPanel.java       Budget cards with progress bars and set-budget dialog
    ├── ReportPanel.java       Reports screen: monthly bar chart + top expenses
    └── ReportPrinter.java     CLI-era text report generator (kept for reference)

expenses.csv                   Auto-generated — stores all your expense entries
budgets.csv                    Auto-generated — stores your category budget limits
```

---

## Getting Started

### Requirements
- Java JDK 11 or higher
- IntelliJ IDEA (recommended) or any Java IDE

### Setup

**1. Clone the repository**
```bash
git clone <your-repo-url>
cd Expense-Tracker
```

**2. Open in IntelliJ**
- File → Open → select the project folder
- If asked, mark `src/` as Sources Root (right-click → Mark Directory as → Sources Root)

**3. Run**
- Open `src/com/expenseTracker/Main.java`
- Click the green run button or press `Shift + F10`

The app window opens at 1100×720 and is resizable down to 900×600.

---

## How to Use

### Adding an expense
Click **+ Add Expense** on the Dashboard or the Expenses screen. Fill in:
- **Amount** — in rupees (₹)
- **Category** — choose from the dropdown or type a new one
- **Description** — optional note

The date is recorded automatically as today.

### Setting a budget
Go to **Budgets** → click **+ Set Budget**. Pick a category and enter a limit. From then on, every time you add an expense in that category, the app checks your remaining balance and warns you if you're over.

### Viewing reports
Go to **Reports** to see your monthly bar chart and top expenses. The chart always shows the last 6 months.

### Saving your data
Data is auto-saved when you close the app. You can also click **Save Now** at the bottom of the sidebar at any time.

---

## Data Storage

Expenses are saved in `expenses.csv` in the project root:

```
id,amount,category,date,description
1,250.00,Food,2025-04-11,"Lunch at canteen"
2,1200.00,Travel,2025-04-11,"Train ticket to Mumbai"
```

Budgets are saved in `budgets.csv`:

```
category,limit
Food,3000.00
Travel,5000.00
```

Both files are plain text and can be opened in Excel or Google Sheets if needed. They are excluded from version control in `.gitignore` since they contain personal data.

---

## Category Colors

Expensio assigns a consistent color to each category across all screens:

| Category      | Color  |
|---------------|--------|
| Food          | Red    |
| Travel        | Blue   |
| Shopping      | Purple |
| Health        | Green  |
| Entertainment | Amber  |
| Education     | Teal   |
| Bills         | Dark Red |
| Other         | Grey   |

Custom categories automatically get a unique color generated from the category name.

---

## Tech Stack

| Layer       | Technology                        |
|-------------|-----------------------------------|
| Language    | Java 11+                          |
| UI          | Java Swing (no external libraries)|
| Charts      | Custom-drawn with `Graphics2D`    |
| Storage     | Plain CSV files                   |
| Build       | IntelliJ (no Maven/Gradle)        |

---

*Built with Java Swing — no external dependencies required*