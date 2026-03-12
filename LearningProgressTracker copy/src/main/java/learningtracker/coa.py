import ttkbootstrap as tb
from ttkbootstrap.constants import *
from tkinter import messagebox, END
from tkinter.scrolledtext import ScrolledText
import math
import time
import threading
from PIL import Image, ImageDraw, ImageFont, ImageTk, ImageColor, ImageFilter
import tkinter as tk
import tkinter.font as tkfont
import os
import urllib.request
from io import BytesIO
try:
    import winsound  # Windows beep support
except Exception:
    winsound = None

# ---------------- Utility ----------------
def is_valid_binary(num, bits=4):
    return len(num) == bits and all(bit in '01' for bit in num)

def get_ab(bits=4):
    a = entry_a.get().strip()
    b = entry_b.get().strip()
    if not is_valid_binary(a, bits) or not is_valid_binary(b, bits):
        messagebox.showerror("Invalid Input", f"Enter valid {bits}-bit binary numbers (e.g. 1010).")
        return None
    return a, b

def unsigned_decimal(bin_str):
    return int(bin_str, 2)

def signed_decimal(bin_str):
    bits = len(bin_str)
    val = int(bin_str, 2)
    if bin_str[0] == '0':
        return val
    else:
        return val - (1 << bits)

def update_flags(result_bin, carry=0, overflow=0):
    if not result_bin:
        flags_label.configure(text="Flags: Z- S- C- O-")
        return
    Z = '1' if all(bit == '0' for bit in result_bin) else '0'
    S = result_bin[0] if len(result_bin) > 0 else '0'
    C = str(carry)
    O = str(overflow)
    flags_label.configure(text=f"Flags: Z-{Z} S-{S} C-{C} O-{O}")

def show_output(bin_result, explanation, carry=0, overflow=0):
    if not bin_result:
        output_label.configure(text="Binary: -    Decimal: -")
    else:
        bin_str = bin_result
        try:
            udec = unsigned_decimal(bin_str)
        except Exception:
            udec = "-"
        try:
            sdec = signed_decimal(bin_str)
        except Exception:
            sdec = "-"
        output_label.configure(
            text=f"Binary: {bin_str}    Dec (unsigned): {udec}    Dec (two's): {sdec}"
        )
    # Global explanation area removed (per user request). Per-tab explanations updated below.
    # Update per-tab explanation widgets if they exist (Arithmetic & Division)
    try:
        if 'arith_explanation_text' in globals():
            arith_explanation_text.config(state='normal')
            arith_explanation_text.delete(1.0, END)
            arith_explanation_text.insert(END, explanation if explanation else "")
            arith_explanation_text.config(state='disabled')
    except Exception:
        pass
    update_flags(bin_result, carry, overflow)
    # Trigger a short attention animation on result labels
    try:
        animate_result_flash()
    except Exception:
        pass

def animate_result_flash(duration=600, steps=6):
    # Animations disabled for a static UI — placeholder kept for compatibility
    return

# Lightweight UI helpers (no-op/compatibility versions)
def add_hover_scale(widget, scale=1.08):
    """Compatibility stub: attach harmless hover/press handlers if desired."""
    try:
        # no visual change, but keep binding to avoid errors
        widget.bind('<Enter>', lambda e: None)
    except Exception:
        pass

def add_genie_effect(button_widget, command_func, target_widget=None, duration=600):
    """Compatibility: attach the command directly so buttons work without animations."""
    try:
        button_widget.configure(command=command_func)
    except Exception:
        try:
            button_widget.bind('<Button-1>', lambda _=None: command_func())
        except Exception:
            pass
 
_beep_playing = False
_beep_lock = threading.Lock()

def play_beep(count: int = 10, low_freq: int = 1200, high_freq: int = 2400, tone_ms: int = 150, gap_ms: int = 30):
    """Play a siren-like sequence of beeps (non-blocking).
    - count: number of beeps (5–10 recommended)
    - low_freq/high_freq: frequency sweep range (Hz)
    - tone_ms: duration of each beep
    - gap_ms: silence between beeps
    Only one siren plays at a time; new requests while one is playing are ignored.
    """
    def _siren():
        global _beep_playing
        try:
            # Build an up-then-down frequency pattern for a siren feel
            half = max(2, count // 2)
            up = [int(low_freq + (high_freq - low_freq) * (i / (half - 1))) for i in range(half)]
            down = up[-2:0:-1] if count > 2 else []
            freqs = (up + down)[:count] if count > 2 else up[:count]

            if winsound is not None:
                for f in freqs:
                    try:
                        winsound.Beep(max(37, min(32767, f)), max(50, tone_ms))
                    except Exception:
                        # If Beep fails, try message beep or a system alias asynchronously
                        try:
                            winsound.MessageBeep(getattr(winsound, 'MB_ICONEXCLAMATION', -1))
                        except Exception:
                            try:
                                winsound.PlaySound("SystemHand", winsound.SND_ALIAS | getattr(winsound, 'SND_ASYNC', 1))
                            except Exception:
                                pass
                        # Keep timing consistent for the sequence
                        time.sleep(tone_ms / 1000.0)
                    time.sleep(gap_ms / 1000.0)
            else:
                # Fallback: use Tk bell repeatedly
                for _ in freqs:
                    try:
                        if 'app' in globals():
                            app.bell()
                    except Exception:
                        pass
                    time.sleep((tone_ms + gap_ms) / 1000.0)
        finally:
            with _beep_lock:
                _beep_playing = False

    with _beep_lock:
        global _beep_playing
        if _beep_playing:
            return
        _beep_playing = True

    try:
        threading.Thread(target=_siren, daemon=True).start()
    except Exception:
        with _beep_lock:
            _beep_playing = False

# ---------------- Core Helper ----------------
def ripple_add_bits(a, b, bits=4):
    a = a.zfill(bits)
    b = b.zfill(bits)
    carry = 0
    result_bits = []
    explanation_lines = []
    for i in range(bits - 1, -1, -1):
        A = int(a[i])
        B = int(b[i])
        sum_bit = A ^ B ^ carry
        carry_out = (A & B) | (carry & (A ^ B))
        result_bits.insert(0, str(sum_bit))
        explanation_lines.append(f"Bit {bits-1-i} (pos {i}): {A}+{B}+cin={carry} => sum={sum_bit}, cout={carry_out}")
        carry = carry_out
    overflow = (int(a[0]) == int(b[0])) and (int(result_bits[0]) != int(a[0]))
    return ''.join(result_bits), "\n".join(explanation_lines), carry, int(overflow)

# ---------------- Logic Operations (Fully Educational Version) ----------------

# Logic operations removed per user request (AND/OR/XOR/NOT/NAND/NOR)



# ---------------- Arithmetic Operations (Fully Educational Version) ----------------
def do_add():
    pair = get_ab()
    if not pair: 
        return
    a, b = pair
    # Immediate feedback beep on button press
    try:
        play_beep(10)
    except Exception:
        pass
    res, steps, carry, overflow = ripple_add_bits(a, b)
    explanation_lines = []
    explanation_lines.append(f"Addition — teaching steps for adding A = {a} and B = {b} (4-bit).\n")
    explanation_lines.append("We perform binary addition starting from the least significant bit (rightmost). For each bit we will show the two input bits, the carry-in, the resulting sum bit and the carry-out.")

    for idx, line in enumerate(steps.split("\n")):
        # each 'line' describes a bit operation from ripple_add_bits
        explanation_lines.append(f"Step {idx+1}: {line}")

    explanation_lines.append(f"\nFinal Sum (4 bits) = {res}.")
    explanation_lines.append(f"Carry out after MSB = {carry}.")
    explanation_lines.append(f"Overflow flag (if addition exceeded signed range) = {overflow}.")
    explanation_lines.append("\nExplanation for beginners:")
    explanation_lines.append("  - Start from the rightmost (least significant) bit.")
    explanation_lines.append("  - Add the two bits and the carry-in to produce a sum bit and carry-out.")
    explanation_lines.append("  - The carry-out is passed to the next more significant bit.")
    explanation_lines.append("  - Overflow occurs when the sign bit cannot represent the true signed sum.")

    show_output(res, "\n".join(explanation_lines), carry, overflow)

def do_sub():
    pair = get_ab()
    if not pair: 
        return
    a, b = pair
    # Immediate feedback beep on button press
    try:
        play_beep(10)
    except Exception:
        pass

    explanation_lines = []
    explanation_lines.append(f"Subtraction (A - B) using Two's Complement — teaching steps for A = {a}, B = {b}.")
    explanation_lines.append("High level idea: to subtract B from A we compute A + (two's complement of B). Two's complement flips B's bits and adds 1.")

    # Step 1: Invert B
    b_inv = ''.join('1' if x=='0' else '0' for x in b)
    explanation_lines.append(f"Step 1: Invert B (flip each bit): B = {b} -> inverted = {b_inv}.")

    # Step 2: Add 1 to get two's complement
    b_twos, _, _, _ = ripple_add_bits(b_inv, '0001')
    explanation_lines.append(f"Step 2: Add 1 to inverted B to get Two's Complement: {b_twos}.")

    # Step 3: Add A + Two's complement of B
    sum_res, steps, carry, overflow = ripple_add_bits(a, b_twos)
    explanation_lines.append("Step 3: Add A and Two's Complement of B bit by bit (LSB to MSB):")
    for i, line in enumerate(steps.split("\n")):
        explanation_lines.append(f"  {line}")

    explanation_lines.append(f"\nFinal result (binary) = {sum_res}.")
    explanation_lines.append(f"Carry out = {carry}; Overflow = {overflow}.")
    explanation_lines.append("\nTeaching notes:")
    explanation_lines.append("  - Two's complement is a common way to represent negative numbers in binary.")
    explanation_lines.append("  - Subtraction A - B becomes A + (two's complement of B).")

    show_output(sum_res, "\n".join(explanation_lines), carry, overflow)

def do_mul():
    pair = get_ab(bits=4)
    if not pair: 
        return
    a, b = pair
    # Immediate feedback beep on button press
    try:
        play_beep(10)
    except Exception:
        pass
    m, r = unsigned_decimal(a), unsigned_decimal(b)
    
    product = 0
    explanation_lines = []
    explanation_lines.append(f"Multiplication — shift-and-add method for A = {a} ({m}), B = {b} ({r}).")
    explanation_lines.append("High level: examine each bit of the multiplier (B) from least significant to most. If the bit is 1, add the multiplicand A shifted left by that bit position to the running product.")

    for i in range(4):
        bit = (r >> i) & 1
        explanation_lines.append(f"Step {i+1}: Check multiplier bit {i} (from LSB=0 to MSB=3). Bit value = {bit}.")
        if bit == 1:
            add_val = m << i
            explanation_lines.append(f"  Bit is 1. Add A shifted left by {i} (binary {format(add_val,'08b')}, decimal {add_val}) to product.")
            product += add_val
        else:
            explanation_lines.append(f"  Bit is 0. Do not add; product remains unchanged.")
        explanation_lines.append(f"  Intermediate product (8-bit view) = {format(product & 0xFF,'08b')} (decimal {product & 0xFF}).")

    res = format(product & 0xFF, '08b')
    explanation_lines.append(f"\nFinal 8-bit product = {res} (decimal {product & 0xFF}).")
    explanation_lines.append("Teaching notes:")
    explanation_lines.append("  - Multiply by checking bits of multiplier and shifting the multiplicand accordingly.")
    explanation_lines.append("  - We use 8 bits for the result to capture overflow from multiplying two 4-bit numbers.")

    show_output(res, "\n".join(explanation_lines))

def do_div():
    pair = get_ab(bits=4)
    if not pair:
        return
    a, b = pair
    # Immediate feedback beep on button press
    try:
        play_beep(10)
    except Exception:
        pass
    dividend, divisor = unsigned_decimal(a), unsigned_decimal(b)

    if divisor == 0:
        messagebox.showerror("Divide by zero", "Divisor cannot be 0")
        return

    # Use standard Restoring Division with A–Q pair shift
    n = 4
    A_reg = 0           # Remainder accumulator (can go negative during trial subtract)
    Q_reg = dividend    # Holds dividend initially; becomes quotient
    M = divisor         # Divisor
    mask_n = (1 << n) - 1

    explanation_lines = []
    explanation_lines.append(f"Restoring Division — A–Q pair shifting for A(dividend) = {a} ({dividend}), M(divisor) = {b} ({divisor}).\n")
    explanation_lines.append("Algorithm per step (MSB to LSB, total n shifts):")
    explanation_lines.append("  1) Shift left the pair (A,Q): A := (A<<1) | msb(Q); Q := (Q<<1).")
    explanation_lines.append("  2) A := A - M (trial subtraction).")
    explanation_lines.append("  3) If A < 0 then restore A := A + M and set Q0 := 0; else set Q0 := 1.")
    explanation_lines.append("")

    for step in range(1, n + 1):
        msb_Q = (Q_reg >> (n - 1)) & 1
        # Step 1: shift (A,Q) left by 1
        A_reg = (A_reg << 1) | msb_Q
        Q_reg = (Q_reg << 1) & mask_n
        explanation_lines.append(f"Step {step}: Shift (A,Q) left. msb(Q)={msb_Q} -> A={format(A_reg if A_reg>=0 else (A_reg & 0x1F), '05b')} ; Q={format(Q_reg, '04b')}")

        # Step 2: trial subtract
        trial = A_reg - M
        explanation_lines.append(f"  Trial subtract A-M: {A_reg} - {M} = {trial} (A'={format(trial if trial>=0 else (trial & 0x1F), '05b')})")

        if trial < 0:
            # restore and set Q0 = 0
            explanation_lines.append("  A' < 0, restore A := A + M and set Q0 = 0")
            A_reg = A_reg  # restore by undoing subtract -> equivalently keep old A_reg
            # More explicit restore:
            A_reg = A_reg + 0  # no change (kept for clarity)
            # Q0 already 0 due to shift
        else:
            # keep trial result and set Q0 = 1
            A_reg = trial
            Q_reg = Q_reg | 0x1
            explanation_lines.append(f"  A' >= 0, keep A := {A_reg} and set Q0 = 1 -> Q={format(Q_reg, '04b')}")

    qbin = format(Q_reg & mask_n, '04b')
    rbin = format(A_reg & mask_n, '04b')
    explanation_lines.append('\nSummary:')
    explanation_lines.append(f"  Quotient Q = {qbin} (decimal {int(qbin, 2)})")
    explanation_lines.append(f"  Remainder A = {rbin} (decimal {int(rbin, 2)})")

    show_output(qbin, "\n".join(explanation_lines))


# Shift/Rotate operations removed per user request (UI and logic deleted)



# ---------------- GUI ----------------
app = tb.Window(themename="cosmo")   # Apple-like clean theme
app.title(" Ultimate 4-bit ALU")
app.geometry("1100x800")
app.configure(bg="#1e1e1e")  # Dark grey background (macOS style)

# (Background animation removed to keep UI static and clear)

# Global animation framerate (approx 60fps)
FRAME_MS = 16

# Button animation state
_btn_anim_data = {}


# ---------------- Fancy San Francisco Gradient Title ----------------
def make_handwriting_title(master, text, font_size=120,
                           gradient_colors=("#00ffff", "#00bcd4", "#ffffff")):
    """
    Creates a large gradient glowing title using SF Pro Display if available.
    Falls back to Arial if SF font is missing.
    """
    try:
        font = ImageFont.truetype("SF-Pro-Display-Bold.otf", font_size)
    except Exception:
        try:
            font = ImageFont.truetype("Arial.ttf", font_size)
        except Exception:
            font = ImageFont.load_default()

    # Measure text size
    dummy_img = Image.new("RGBA", (10, 10))
    draw = ImageDraw.Draw(dummy_img)
    try:
        bbox = draw.textbbox((0, 0), text, font=font)
        w, h = bbox[2] - bbox[0], bbox[3] - bbox[1]
    except Exception:
        w, h = draw.textsize(text, font=font)

    if w <= 0: w = max(10, len(text) * (font_size // 2))
    if h <= 0: h = font_size + 10

    # Gradient fill
    gradient = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    gdraw = ImageDraw.Draw(gradient)
    colors = [ImageColor.getrgb(c) for c in gradient_colors]
    for y in range(h):
        ratio = y / (h - 1) if h > 1 else 0
        if ratio < 0.5:
            t = ratio * 2
            r = int(colors[0][0] + (colors[1][0] - colors[0][0]) * t)
            g = int(colors[0][1] + (colors[1][1] - colors[0][1]) * t)
            b = int(colors[0][2] + (colors[1][2] - colors[0][2]) * t)
        else:
            t = (ratio - 0.5) * 2
            r = int(colors[1][0] + (colors[2][0] - colors[1][0]) * t)
            g = int(colors[1][1] + (colors[2][1] - colors[1][1]) * t)
            b = int(colors[1][2] + (colors[2][2] - colors[1][2]) * t)
        gdraw.line([(0, y), (w, y)], fill=(r, g, b, 255))

    # Mask for text
    mask = Image.new("L", (w, h), 0)
    mdraw = ImageDraw.Draw(mask)
    mdraw.text((0, 0), text, font=font, fill=255)

    # Glow shadow
    glow = Image.new("RGBA", (w + 20, h + 20), (0, 0, 0, 0))
    shadow = Image.new("RGBA", (w, h), (0, 0, 0, 150))
    glow.paste(shadow, (10, 10), mask)

    gradient.putalpha(mask)
    glow.paste(gradient, (0, 0), gradient)

    imgtk = ImageTk.PhotoImage(glow)
    # Use a static Canvas for the title so it remains clear and visible
    canvas = tk.Canvas(master, width=w+20, height=h+20,
                       bg="#1e1e1e", highlightthickness=0)
    canvas.create_image((w+20)//2, (h+20)//2, image=imgtk, tags=('title_img',))
    canvas.image = imgtk
    return canvas

# Apple-style big heading (static label for crisp rendering)
try:
    # Reduced size so it doesn't dominate the UI
    title_font = ("Segoe UI", 26, "bold")
except Exception:
    title_font = ("Arial", 26, "bold")
title = tk.Label(app, text="4-bit ALU Simulator", font=title_font, bg="#1e1e1e", fg="#ffffff")
title.pack(pady=(12, 10))

# ---------------- Input Frame ----------------
input_frame = tb.Frame(app, padding=18, bootstyle="secondary")  # Sleek frame
input_frame.pack(fill='x', padx=25, pady=(0, 15))

# Label + Entry for A
tb.Label(input_frame, text="Enter A (4-bit):",
         font=("SF Pro Display", 14), foreground="#f0f0f0",
         background="#1e1e1e").grid(row=0, column=0, sticky='w')
entry_a = tb.Entry(input_frame, width=10, font=("SF Pro Display", 16))
entry_a.grid(row=0, column=1, padx=8)

# Label + Entry for B
tb.Label(input_frame, text="Enter B (4-bit):",
         font=("SF Pro Display", 14), foreground="#f0f0f0",
         background="#1e1e1e").grid(row=0, column=2, sticky='w')
entry_b = tb.Entry(input_frame, width=10, font=("SF Pro Display", 16))
entry_b.grid(row=0, column=3, padx=8)

# Frame for buttons (reset, run, etc.)
btn_frame = tb.Frame(input_frame, bootstyle="secondary")
btn_frame.grid(row=0, column=4, padx=25)

# ---------------- Notebook ----------------
notebook = tb.Notebook(app, bootstyle="warning")
notebook.pack(fill='both', expand=True, padx=20, pady=10)

# Logic operations and UI removed per user request

# Arithmetic Tab
arith_tab = tb.Frame(notebook, padding=12)
notebook.add(arith_tab, text="⚡ Arithmetic")
# Use a split for Arithmetic tab as well
arith_paned = tk.PanedWindow(arith_tab, orient=tk.HORIZONTAL)
arith_paned.pack(fill='both', expand=True)

arith_left = tb.Frame(arith_paned, padding=8, bootstyle='secondary')
arith_right = tb.Frame(arith_paned, padding=8)
arith_paned.add(arith_left)
arith_paned.add(arith_right, minsize=420)

add_btn = tb.Button(arith_left, text="ADD", command=do_add)
add_btn.grid(row=0, column=0, padx=5, pady=5)
sub_btn = tb.Button(arith_left, text="SUB", command=do_sub)
sub_btn.grid(row=0, column=1, padx=5, pady=5)
mul_btn = tb.Button(arith_left, text="MUL", command=do_mul)
mul_btn.grid(row=0, column=2, padx=5, pady=5)
div_btn = tb.Button(arith_left, text="DIV", command=do_div)
div_btn.grid(row=0, column=3, padx=5, pady=5)
for b in (add_btn, sub_btn, mul_btn, div_btn):
    add_hover_scale(b)

# Right side: local result and explanation for arithmetic
arith_output_label = tk.Label(arith_right, text="Result: -", font=("Consolas", 11))
arith_output_label.pack(anchor='w', pady=(8, 4))
arith_explanation_text = ScrolledText(arith_right, height=12, font=("Consolas", 10), bg="#ffffff", fg="#111111", wrap='word')
arith_explanation_text.pack(fill='both', expand=True)
arith_explanation_text.config(state='disabled')

# Shift & Rotate tab removed per user request

# CLA vs Ripple Demo removed per user request (UI and logic deleted)

# ---------------- Division Techniques Tab ----------------
divtech_tab = tb.Frame(notebook, padding=12)
notebook.add(divtech_tab, text="➗ Division Tech")

def do_restoring_div():
    pair = get_ab(bits=4)
    if not pair:
        return
    a, b = pair
    # Immediate feedback beep on button press
    try:
        play_beep(10)
    except Exception:
        pass
    dividend = int(a, 2)
    divisor = int(b, 2)
    if divisor == 0:
        messagebox.showerror("Divide by zero", "Divisor cannot be 0")
        return

    # Standard Restoring Division using A–Q pair left shifts
    n = 4
    A_reg = 0
    Q_reg = dividend
    M = divisor
    mask_n = (1 << n) - 1
    explanation = []
    explanation.append("Restoring Division — A–Q pair shifting (didactic):\n")
    explanation.append("We process MSB to LSB for n=4 steps. Each step:\n")
    explanation.append("  1) Shift (A,Q) left: A := (A<<1) | msb(Q); Q := (Q<<1).\n")
    explanation.append("  2) A := A - M (trial subtract).\n")
    explanation.append("  3) If A < 0 then restore A and set Q0=0; else keep A and set Q0=1.\n")
    explanation.append(f"Inputs: Dividend Q0..Q3 = {a} ({dividend}), Divisor M = {b} ({divisor})\n")

    for step in range(1, n + 1):
        msb_Q = (Q_reg >> (n - 1)) & 1
        A_reg = (A_reg << 1) | msb_Q
        Q_reg = (Q_reg << 1) & mask_n
        explanation.append(f"Step {step}: Shift (A,Q). msb(Q)={msb_Q} -> A={format(A_reg if A_reg>=0 else (A_reg & 0x1F), '05b')} ; Q={format(Q_reg, '04b')}")

        trial = A_reg - M
        explanation.append(f"  Trial subtract: A-M = {A_reg}-{M} = {trial} (A'={format(trial if trial>=0 else (trial & 0x1F), '05b')})")

        if trial < 0:
            explanation.append("  A' < 0 -> restore A (undo subtract), Q0 stays 0")
            # restore by keeping A_reg as-is (undo the subtract)
        else:
            A_reg = trial
            Q_reg = Q_reg | 0x1
            explanation.append(f"  A' >= 0 -> keep A={A_reg}, set Q0=1 -> Q={format(Q_reg, '04b')}")

    qbin = format(Q_reg & mask_n, '04b')
    rbin = format(A_reg & mask_n, '04b')
    explanation.append('\nSummary:')
    explanation.append(f"  Final Quotient Q = {qbin} (decimal {int(qbin,2)})")
    explanation.append(f"  Final Remainder A = {rbin} (decimal {int(rbin,2)})")

    # Show results in main output area
    show_output(qbin, '\n'.join(explanation))

    # Update inline division panel (right-hand side of Division tab)
    try:
        div_output_label.configure(text=f"Quotient: {qbin}    Remainder: {rbin}")
        div_explanation_text.config(state='normal')
        div_explanation_text.delete(1.0, END)
        div_explanation_text.insert(END, '\n'.join(explanation))
        div_explanation_text.config(state='disabled')
    except Exception:
        pass

    # Try to display the exact restoring division flowchart image linked by the user.
    # Strategy: prefer a local file 'restoring_division.ppm' in the project; otherwise try to fetch from the URL.
    try:
        # Prefer a local PNG (easy to inspect) then a PPM. If neither exists, attempt to fetch the PPM URL and save it locally as PNG.
        flow_url = 'https://www.researchgate.net/profile/Manpreet-Manna/publication/250612202/figure/fig3/AS:667661163847693@1536194234990/Flow-chart-of-Restoring-Division-Algorithm.ppm'
        base_dir = os.path.dirname(_file) if 'file_' in globals() else os.getcwd()
        local_png = os.path.join(base_dir, 'restoring_division.png')
        local_ppm = os.path.join(base_dir, 'restoring_division.ppm')
        img = None

        # Try PNG first
        if os.path.exists(local_png):
            try:
                img = Image.open(local_png)
            except Exception:
                img = None

        # Then try PPM
        if img is None and os.path.exists(local_ppm):
            try:
                img = Image.open(local_ppm)
            except Exception:
                img = None

        # Otherwise try fetching the PPM and save as PNG for future fast loads
        if img is None:
            try:
                with urllib.request.urlopen(flow_url, timeout=8) as resp:
                    data = resp.read()
                    img = Image.open(BytesIO(data))
                    # attempt to save as PNG locally
                    try:
                        img.save(local_png, format='PNG')
                    except Exception:
                        pass
            except Exception:
                img = None

        if img is not None:
            # Resize image to fit available width
            try:
                target_w = div_right.winfo_width() or 600
                if target_w < 100: target_w = 600
            except Exception:
                target_w = 600
            try:
                scale = float(target_w) / float(img.width)
                target_h = max(80, int(img.height * scale))
                img_resized = img.resize((int(target_w), int(target_h)), Image.LANCZOS)
            except Exception:
                img_resized = img

            try:
                imgtk = ImageTk.PhotoImage(img_resized)
                if hasattr(div_right, 'flowchart_label') and isinstance(div_right.flowchart_label, tk.Label):
                    div_right.flowchart_label.configure(image=imgtk)
                    div_right.flowchart_label.image = imgtk
                else:
                    lbl = tk.Label(div_right, image=imgtk)
                    lbl.image = imgtk
                    lbl.pack(fill='both', expand=False, pady=(2,6))
                    div_right.flowchart_label = lbl
            except Exception:
                pass
    except Exception:
        pass

def do_nonrestoring_div():
    pair = get_ab(bits=4)
    if not pair:
        return
    a, b = pair
    # Immediate feedback beep on button press
    try:
        play_beep(10)
    except Exception:
        pass
    dividend = int(a, 2)
    divisor = int(b, 2)
    if divisor == 0:
        messagebox.showerror("Divide by zero", "Divisor cannot be 0")
        return

    n = 4
    A = dividend
    Q = 0
    R = 0
    explanation = []
    # Teaching-style introduction for Non-Restoring Division
    explanation.append("Non-Restoring Division — beginner-friendly steps:\n")
    explanation.append("This method avoids restoring the remainder on every failed subtraction. Instead, it keeps track of the sign of R and either adds or subtracts the divisor depending on that sign.\n")
    explanation.append("We will process each dividend bit from MSB to LSB:\n")
    explanation.append("  1) Shift R left and bring down the next bit of the dividend.\n")
    explanation.append("  2) If the previous R was non-negative, subtract the divisor. If the previous R was negative, add the divisor.\n")
    explanation.append("  3) Set the current quotient bit to 1 if the new R is non-negative, otherwise set it to 0.\n")
    explanation.append(f"Inputs: Dividend = {a} (decimal {A}), Divisor = {b} (decimal {divisor})\n")

    for step_num, i in enumerate(range(n-1, -1, -1), start=1):
        prev_R = R
        # Shift left (R,Q)
        R = (R << 1) | ((A >> i) & 1)
        explanation.append(f"Step {step_num}: Shift R left and bring down dividend bit A[{i}].")
        explanation.append(f"  After shift, R = {format(R, '04b')} (binary) = {R} (decimal)")

        # Decide operation based on sign of previous R
        # For clarity we check prev_R (the R before deciding operation)
        if prev_R >= 0:
            # previous R was non-negative -> subtract divisor now
            R = R - divisor
            explanation.append(f"  Previous R was non-negative, so subtract divisor: R = R - M -> {format(R & 0xF, '04b')} (decimal {R})")
        else:
            # previous R was negative -> add divisor now
            R = R + divisor
            explanation.append(f"  Previous R was negative, so add divisor: R = R + M -> {format(R & 0xF, '04b')} (decimal {R})")

        # Set quotient bit depending on new R sign
        qbit = 1 if R >= 0 else 0
        Q = (Q << 1) | qbit
        explanation.append(f"  Because the new R is {'non-negative' if R >= 0 else 'negative'}, set current quotient bit = {qbit}.")

    # Final correction: if remainder is negative, make it positive by adding divisor
    if R < 0:
        explanation.append("Final correction: remainder is negative after main loop, so add divisor to correct it.")
        R = R + divisor
        explanation.append(f"  Corrected R = {format(R & 0xF, '04b')} (decimal {R})")

    qbin = format(Q & 0xF, '04b')
    rbin = format(R & 0xF, '04b')
    explanation.append('\nSummary:')
    explanation.append(f"  Final Quotient (4 bits) = {qbin} (decimal {int(qbin,2)})")
    explanation.append(f"  Final Remainder (4 bits) = {rbin} (decimal {int(rbin,2)})")

    # Show results in main output area
    show_output(qbin, '\n'.join(explanation))

    # Update inline division panel (right-hand side of Division tab)
    try:
        div_output_label.configure(text=f"Quotient: {qbin}    Remainder: {rbin}")
        div_explanation_text.config(state='normal')
        div_explanation_text.delete(1.0, END)
        div_explanation_text.insert(END, '\n'.join(explanation))
        div_explanation_text.config(state='disabled')
    except Exception:
        pass


# UI for Division Techniques — split left (options) / right (flowchart + result)
div_paned = tk.PanedWindow(divtech_tab, orient=tk.HORIZONTAL)
div_paned.pack(fill='both', expand=True)

div_left = tb.Frame(div_paned, padding=8, bootstyle='secondary')
div_right = tb.Frame(div_paned, padding=8)
div_paned.add(div_left)
div_paned.add(div_right, minsize=420)

# Left: controls
rest_btn = tb.Button(div_left, text="Restoring", command=do_restoring_div)
rest_btn.pack(fill='x', pady=6)
nonrest_btn = tb.Button(div_left, text="Non-Restoring", command=do_nonrestoring_div)
nonrest_btn.pack(fill='x', pady=6)
for b in (rest_btn, nonrest_btn):
    add_hover_scale(b)

# Right: flowchart area left intentionally empty; explanations and results will appear below

# Local result display on the right
# Safely obtain background color for div_right (ttk/ttkbootstrap widgets may not expose 'background')
try:
    bg = div_right.cget('background')
except Exception:
    try:
        bg = div_right.cget('bg')
    except Exception:
        bg = None

label_kwargs = {"text": "Quotient: -    Remainder: -", "font": ("Consolas", 11)}
if bg is not None:
    label_kwargs["bg"] = bg

div_output_label = tk.Label(div_right, **label_kwargs)
div_output_label.pack(anchor='w', pady=(8, 4))

div_explanation_text = ScrolledText(div_right, height=10, font=("Consolas", 10), bg="#ffffff", fg="#111111", wrap='word')
div_explanation_text.pack(fill='both', expand=True)
div_explanation_text.config(state='disabled')

# Results and Flags (global bottom area)
result_frame = tb.Labelframe(app, text="📊 Result & Flags", padding=10, bootstyle="danger")
result_frame.pack(fill='x', padx=20, pady=10)

output_label = tb.Label(result_frame, text="Binary: -    Decimal: -", font=("Consolas", 12), bootstyle="warning")
output_label.pack(anchor='w')

flags_label = tb.Label(result_frame, text="Flags: Z- S- C- O-", font=("Consolas", 12), bootstyle="warning")
flags_label.pack(anchor='w', pady=(2, 5))

# Global explanation area removed per user request (kept per-tab explanations instead)

# ---------------- Apple-Style Reset Button with Emoji Animation ----------------
def make_reset_button(master, diameter=80):
    canvas = tk.Canvas(
        master,
        width=diameter,
        height=diameter,
        highlightthickness=0,
        bg="#1c1c1e"   # Apple dark grey theme background
    )
    canvas.pack()

    center = diameter // 2
    # Base circle (Apple dark grey style)
    circle = canvas.create_oval(
        6, 6, diameter - 6, diameter - 6,
        fill="#2c2c2e",     # Dark grey to blend with app
        outline="#3a3a3c",  # slightly lighter outline
        width=2
    )

    # Default Reset symbol (↻)
    text = canvas.create_text(
        center, center, text="↻",
        fill="white",
        font=("SF Pro Display", 26, "bold")
    )

    # Hover: show pointer cursor to indicate clickability
    def on_enter(_):
        try:
            canvas.config(cursor='hand2')
        except Exception:
            pass
    def on_leave(_):
        try:
            canvas.config(cursor='')
        except Exception:
            pass

    # ----- Reset Functionality -----
    def on_click(_):
        entry_a.delete(0, END)
        entry_b.delete(0, END)
        output_label.configure(text="Binary: -    Decimal: -")
        flags_label.configure(text="Flags: Z- S- C- O-")
        # CLA demo removed — nothing to clear here
    canvas.bind("<Enter>", on_enter)
    canvas.bind("<Leave>", on_leave)
    canvas.bind("<Button-1>", on_click)
    return canvas
# Place reset button next to input fields
reset_btn_canvas = make_reset_button(btn_frame, diameter=70)

# Attach genie effect to main buttons to add a smooth 'send to result' animation
try:
    # Arithmetic (genie effect hookups)
    add_genie_effect(add_btn, do_add)
    add_genie_effect(sub_btn, do_sub)
    add_genie_effect(mul_btn, do_mul)
    add_genie_effect(div_btn, do_div)
except Exception:
    pass

# Ensure UI widgets are above background and start animation
def _lift_ui():
    try:
        for w in (input_frame, notebook, result_frame, title):
            w.lift()
        reset_btn_canvas.lift()
    except Exception:
        pass

_lift_ui()

app.mainloop()