import os, re, subprocess, time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs' / 'screenshots'
OUT.mkdir(parents=True, exist_ok=True)
PKG = 'com.adit.sirs'
ADMIN_EMAIL='budi@example.com'
ADMIN_PW='Budi12345!'
USER_EMAIL=f'sirsuser{int(time.time())}@example.com'
USER_PW='User12345@abc'
USER_NAME='UserUAS'

os.environ['MSYS_NO_PATHCONV']='1'

def sh(cmd, timeout=20, check=True):
    p = subprocess.run(cmd, shell=True, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=timeout)
    if check and p.returncode != 0:
        raise RuntimeError(f'cmd failed {cmd}\n{p.stdout}')
    return p.stdout

def adb(cmd, timeout=20, check=True):
    parts = ['adb'] + cmd.split()
    p = subprocess.run(parts, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=timeout)
    if check and p.returncode != 0:
        raise RuntimeError(f'adb failed {cmd}\n{p.stdout}')
    return p.stdout

def esc_text(s):
    # Avoid spaces. Android input text accepts @ directly when subprocess is not shell-quoting.
    return s.replace(' ', '%s')

def dump():
    adb('shell uiautomator dump /sdcard/window_dump.xml')
    return adb('exec-out cat /sdcard/window_dump.xml', timeout=10)

def text_contains(txt):
    xml = dump()
    return any(t in xml for t in ([txt] if isinstance(txt,str) else txt)), xml

def screenshot(name):
    path = OUT / name
    with open(path, 'wb') as f:
        p = subprocess.run('adb exec-out screencap -p', shell=True, stdout=f, stderr=subprocess.PIPE, timeout=20)
    if p.returncode != 0 or path.stat().st_size < 1000:
        raise RuntimeError(f'screenshot failed {name}: {p.stderr}')
    return path

def bounds_of_text(xml, patterns):
    if isinstance(patterns, str):
        patterns=[patterns]
    for pat in patterns:
        for node in re.findall(r'<node [^>]+>', xml):
            text_m = re.search(r'(?:text|content-desc)="([^"]*)"', node)
            b_m = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', node)
            if text_m and b_m and pat in text_m.group(1):
                x1,y1,x2,y2=map(int,b_m.groups())
                return (x1+x2)//2,(y1+y2)//2
    return None

def all_edit_bounds(xml):
    out=[]
    for m in re.finditer(r'class="android\.widget\.EditText"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml):
        x1,y1,x2,y2=map(int,m.groups())
        out.append(((x1+x2)//2,(y1+y2)//2, y1, y2))
    return sorted(out, key=lambda t:t[2])

def tap(x,y): adb(f'shell input tap {x} {y}', timeout=5)
def press(key): adb(f'shell input keyevent {key}', timeout=5)
def type_text(s): adb(f'shell input text "{esc_text(s)}"', timeout=5)
def wait_text(targets, timeout=20):
    end=time.time()+timeout
    last=''
    while time.time()<end:
        ok,last=text_contains(targets)
        if ok: return last
        time.sleep(1)
    return last

def tap_text(patterns, timeout=10):
    xml = wait_text(patterns, timeout)
    pos = bounds_of_text(xml, patterns)
    if not pos:
        raise RuntimeError(f'no text bounds {patterns}\n{xml[:1000]}')
    tap(*pos); time.sleep(1)
    return pos

def fill_visible_edits(values):
    xml=dump(); fields=all_edit_bounds(xml)
    if len(fields)<len(values):
        raise RuntimeError(f'need {len(values)} edit fields, got {len(fields)}')
    for (x,y,_,_), val in zip(fields, values):
        tap(x,y); time.sleep(.2); type_text(val); time.sleep(.3)
    press('KEYCODE_BACK'); time.sleep(.5)

def launch_clean():
    adb(f'shell pm clear {PKG}', timeout=10)
    adb(f'shell monkey -p {PKG} -c android.intent.category.LAUNCHER 1', timeout=10)
    time.sleep(4)
    return dump()

def launch_keep():
    adb(f'shell monkey -p {PKG} -c android.intent.category.LAUNCHER 1', timeout=10)
    time.sleep(2)
    return dump()

results=[]

# User registration flow
xml=launch_clean()
wait_text(['Login','Masuk'], 25)
results.append(('01_login_awal.png', screenshot('01_login_awal.png'), 'Layar login awal aplikasi SIRS.'))
tap_text(['Daftar','Registrasi','Buat Akun'], 8)
wait_text('Daftar Akun', 10)
# Fill top visible fields: name,email,password ; then scroll for confirm if needed
xml=dump()
fields=all_edit_bounds(xml)
# register screen has 4 fields, Pixel 6 Pro normally shows all
fill_visible_edits([USER_NAME, USER_EMAIL, USER_PW, USER_PW])
xml=dump()
results.append(('02_register_terisi.png', screenshot('02_register_terisi.png'), 'Form registrasi user uji terisi lengkap dengan password memenuhi standar.'))
tap_text('Daftar', 8)
xml=wait_text(['Dashboard','Laporan','Buat Laporan','Profil'], 35)
results.append(('03_dashboard_user.png', screenshot('03_dashboard_user.png'), 'Dashboard pengguna setelah registrasi akun user uji berhasil.'))
# User report list or create report screen
try:
    tap_text(['Buat Laporan','Tambah Laporan'], 5)
    wait_text(['Buat Laporan','Judul','Kategori'], 10)
    results.append(('04_form_buat_laporan_user.png', screenshot('04_form_buat_laporan_user.png'), 'Form pembuatan laporan insiden dari role user.'))
    press('KEYCODE_BACK'); time.sleep(1)
except Exception as e:
    print('WARN create form user:', e)
try:
    tap_text(['Laporan Saya','Daftar Laporan','Laporan'], 5)
    wait_text(['Laporan','Status','Filter','Belum ada'], 10)
    results.append(('05_daftar_laporan_user.png', screenshot('05_daftar_laporan_user.png'), 'Daftar laporan milik pengguna dan area filter/status.'))
except Exception as e:
    print('WARN list user:', e)

# Logout if possible, then admin login
adb(f'shell pm clear {PKG}', timeout=10)
adb(f'shell monkey -p {PKG} -c android.intent.category.LAUNCHER 1', timeout=10)
time.sleep(3)
wait_text(['Login','Masuk'], 15)
xml=dump(); fields=all_edit_bounds(xml)
if len(fields) < 2: raise RuntimeError('login fields not found')
for (x,y,_,_), val in zip(fields[:2], [ADMIN_EMAIL, ADMIN_PW]):
    tap(x,y); time.sleep(.2); type_text(val); time.sleep(.3)
press('KEYCODE_BACK'); time.sleep(.5)
results.append(('06_login_admin_terisi.png', screenshot('06_login_admin_terisi.png'), 'Form login admin terisi memakai kredensial yang diberikan.'))
tap_text(['Masuk','Login'], 8)
xml=wait_text(['Dashboard Admin','Admin','Kategori','Aktivitas','Semua Laporan'], 35)
results.append(('07_dashboard_admin.png', screenshot('07_dashboard_admin.png'), 'Dashboard admin setelah login berhasil.'))
for pats, fname, cap in [
    (['Semua Laporan','Laporan Masuk','Daftar Laporan'], '08_daftar_laporan_admin.png', 'Daftar seluruh laporan pada role admin.'),
    (['Kategori','Manajemen Kategori'], '09_manajemen_kategori.png', 'Layar manajemen kategori laporan oleh admin.'),
    (['Aktivitas','Log Aktivitas'], '10_log_aktivitas.png', 'Layar activity log untuk audit aktivitas aplikasi.')
]:
    try:
        tap_text(pats, 6)
        wait_text(pats, 10)
        results.append((fname, screenshot(fname), cap))
        press('KEYCODE_BACK'); time.sleep(1)
    except Exception as e:
        print('WARN admin nav', fname, e)

print('USER_EMAIL', USER_EMAIL)
print('CAPTURED')
for _,p,cap in results:
    print(p.name, p.stat().st_size, cap)
