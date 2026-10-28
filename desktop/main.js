const { app, BrowserWindow, shell } = require("electron");

const START_URL = "https://campusdesk-student-management.vercel.app";
const ALLOWED_HOSTS = new Set([
  "campusdesk-student-management.vercel.app",
  "campusdesk-student-management.onrender.com"
]);

function isAllowed(urlString) {
  try {
    const url = new URL(urlString);
    return url.protocol === "https:" && ALLOWED_HOSTS.has(url.hostname);
  } catch {
    return false;
  }
}

function createWindow() {
  const win = new BrowserWindow({
    width: 1360,
    height: 900,
    minWidth: 960,
    minHeight: 650,
    title: "CampusDesk",
    backgroundColor: "#f8fafc",
    autoHideMenuBar: true,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      sandbox: true
    }
  });

  win.webContents.setWindowOpenHandler(({ url }) => {
    if (isAllowed(url)) {
      return { action: "allow" };
    }
    if (url.startsWith("https://") || url.startsWith("http://")) {
      shell.openExternal(url);
    }
    return { action: "deny" };
  });

  win.webContents.on("will-navigate", (event, url) => {
    if (!isAllowed(url)) {
      event.preventDefault();
      if (url.startsWith("https://") || url.startsWith("http://")) {
        shell.openExternal(url);
      }
    }
  });

  win.loadURL(START_URL);
}

app.whenReady().then(() => {
  createWindow();
  app.on("activate", () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") app.quit();
});
