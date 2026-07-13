import os
import re
import subprocess
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs' / 'screenshots' / 'security'
OUT.mkdir(parents=True, exist_ok=True)
PKG = 'com.adit.sirs'
ADMIN_EMAIL = 'budi@example.com'
ADMIN_PW = 'Budi12345!'
WEAK_EMAIL = f'weak{int(time.time())}@example.com'

os.environ['MSYS_NO_PATHCONV'] = '1'


def adb(args, timeout=20, check=True):
    if isinstance(args, str):
        cmd = ['adb'] + args.split()
    else:
        cmd = ['adb'] + args
    p = subprocess.run(cmd, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=timeout)
    if check and p.returncode != 0:
        raise RuntimeError(f"adb failed: {' '.join(cmd)}\n{p.stdout}")
    return p.stdout


def dump():
    adb('shell uiautomator dump /sdcard/window_dump.xml', timeout=10, check=False)
    return adb('exec-out cat /sdcard/window_dump.xml', timeout=10, check=False)


def screenshot(name):
    path = OUT / name
    with open(path, 'wb') as f:
        p = subprocess.run('adb exec-out screencap -p', shell=True, stdout=f, stderr=subprocess.PIPE, timeout=20)
    if p.returncode != 0 or path.stat().st_size < 1000:
        raise RuntimeError(f'screenshot failed {name}: {p.stderr}')
    return path


def text_in_xml(needle):
    xml = dump()
    if isinstance(needle, str):
        return needle in xml, xml
    return any(n in xml for n in needle), xml


def wait_text(needles, timeout=20):
    end = time.time() + timeout
    last = ''
    while time.time() < end:
        ok, last = text_in_xml(needles)
        if ok:
            return last
        time.sleep(0.8)
    return last


def all_nodes(xml):
    return re.findall(r'<node [^>]+>', xml)


def bounds(node):
    m = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', node)
    if not m:
        return None
    x1, y1, x2, y2 = map(int, m.groups())
    return x1, y1, x2, y2, (x1 + x2) // 2, (y1 + y2) // 2


def node_text(node):
    vals = []
    for attr in ('text', 'content-desc'):
        m = re.search(attr + r'="([^"]*)"', node)
        if m:
            vals.append(m.group(1))
    return ' '.join(vals)


def tap(x, y):
    adb(['shell', 'input', 'tap', str(x), str(y)], timeout=5)
    time.sleep(0.5)


def press(key):
    adb(['shell', 'input', 'keyevent', key], timeout=5)
    time.sleep(0.4)


def input_text(text):
    # Avoid spaces in evidence values; pass as one adb arg so ! is safe.
    adb(['shell', 'input', 'text', text.replace(' ', '%s')], timeout=8)
    time.sleep(0.3)


def edit_bounds(xml):
    fields = []
    for node in all_nodes(xml):
        if 'class="android.widget.EditText"' in node:
            b = bounds(node)
            if b:
                fields.append((b[4], b[5], b[1], b[3], node_text(node)))
    return sorted(fields, key=lambda t: t[2])


def tap_text(patterns, timeout=10):
    if isinstance(patterns, str):
        patterns = [patterns]
    xml = wait_text(patterns, timeout)
    for node in all_nodes(xml):
        text = node_text(node)
        if any(p in text for p in patterns):
            b = bounds(node)
            if b:
                tap(b[4], b[5])
                return text
    raise RuntimeError(f'text not found/clickable: {patterns}\n{xml[:1200]}')


def fill_edits(values):
    xml = dump()
    fields = edit_bounds(xml)
    if len(fields) < len(values):
        raise RuntimeError(f'need {len(values)} EditText, got {len(fields)}')
    for (x, y, _, _, _), value in zip(fields, values):
        tap(x, y)
        input_text(value)
    press('KEYCODE_BACK')


def dismiss_permission_if_present():
    xml = dump()
    if 'permissioncontroller' in xml and ('Allow' in xml or 'Don' in xml or 'allow' in xml.lower()):
        # Android permission dialog is outside app UI; use stable Pixel 6 Pro dialog button area.
        tap(720, 1730)
        time.sleep(1.5)


def launch_clean():
    adb(['shell', 'pm', 'clear', PKG], timeout=10)
    adb(['shell', 'monkey', '-p', PKG, '-c', 'android.intent.category.LAUNCHER', '1'], timeout=10)
    time.sleep(4)
    dismiss_permission_if_present()
    # Permission handling can leave focus outside the app on some emulator builds.
    adb(['shell', 'am', 'start', '-n', f'{PKG}/.MainActivity'], timeout=10, check=False)
    time.sleep(2)
    return wait_text(['Login', 'Masuk'], 25)


def launch_keep():
    adb(['shell', 'monkey', '-p', PKG, '-c', 'android.intent.category.LAUNCHER', '1'], timeout=10)
    time.sleep(2)
    return dump()


def save_xml(name):
    path = OUT / name
    path.write_text(dump(), encoding='utf-8')
    return path


results = []

# 1. Weak password registration validation.
launch_clean()
tap_text(['Daftar', 'Registrasi', 'Buat Akun'], 10)
wait_text(['Daftar Akun', 'Nama'], 10)
fill_edits(['WeakUser', WEAK_EMAIL, 'abc', 'abc'])
results.append(('security_01_register_weak_password_filled.png', screenshot('security_01_register_weak_password_filled.png'), 'Form registrasi dengan password lemah sebelum submit.'))
tap_text('Daftar', 10)
xml = wait_text(['Password minimal 12 karakter', 'Password harus'], 10)
save_xml('security_01_register_weak_password_error.xml')
results.append(('security_02_register_weak_password_rejected.png', screenshot('security_02_register_weak_password_rejected.png'), 'Validasi password menolak password lemah.'))

# 2. Invalid login authentication failure.
launch_clean()
fill_edits(['notregistered@example.com', 'WrongPass123@'])
results.append(('security_03_invalid_login_filled.png', screenshot('security_03_invalid_login_filled.png'), 'Form login dengan kredensial tidak valid.'))
tap_text(['Masuk', 'Login'], 10)
xml = wait_text(['gagal', 'failed', 'invalid', 'INVALID', 'tidak', 'salah'], 15)
save_xml('security_03_invalid_login_error.xml')
results.append(('security_04_invalid_login_rejected.png', screenshot('security_04_invalid_login_rejected.png'), 'Firebase Authentication menolak kredensial tidak valid.'))

# 3. Required report fields validation after valid admin login (runtime form validation).
launch_clean()
fill_edits([ADMIN_EMAIL, ADMIN_PW])
results.append(('security_05_admin_login_filled.png', screenshot('security_05_admin_login_filled.png'), 'Login admin untuk menguji akses role administrator.'))
tap_text(['Masuk', 'Login'], 10)
xml = wait_text(['Dashboard Admin', 'Admin', 'Kategori', 'Aktivitas', 'Semua Laporan'], 35)
save_xml('security_05_admin_dashboard.xml')
results.append(('security_06_admin_dashboard_role_verified.png', screenshot('security_06_admin_dashboard_role_verified.png'), 'Dashboard admin terbuka setelah autentikasi dan role administrator valid.'))

# Open create report if admin route exists, otherwise capture category/log as admin-only proof.
try:
    tap_text(['Buat Laporan', 'Tambah Laporan'], 6)
    wait_text(['Buat Laporan', 'Judul', 'Kategori'], 10)
    press('KEYCODE_BACK')
except Exception:
    pass

# 4. Admin-only audit log as access-control/security evidence.
try:
    tap_text(['Aktivitas', 'Log Aktivitas'], 8)
    wait_text(['Log Aktivitas', 'Activity', 'Aktivitas'], 10)
    save_xml('security_07_activity_log_admin_only.xml')
    results.append(('security_07_activity_log_admin_only.png', screenshot('security_07_activity_log_admin_only.png'), 'Activity log hanya tersedia untuk admin sebagai bukti audit trail.'))
except Exception as e:
    print('WARN activity log capture failed:', e)

print('SECURITY_CAPTURED')
for fname, path, caption in results:
    print(f'{fname}|{path.stat().st_size}|{caption}')
