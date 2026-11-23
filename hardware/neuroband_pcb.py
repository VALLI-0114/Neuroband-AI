"""NeuroBand AI PCBs, generated from PCB_DESIGN_SPEC.md.

Run with KiCad's own Python (it has the pcbnew module):
  "C:\\Program Files\\KiCad\\10.0\\bin\\python.exe" neuroband_pcb.py main place    # parts + planes -> .kicad_pcb + .dsn
  java -jar tools/freerouting-1.9.0.jar -de main/neuroband_main.dsn -do main/neuroband_main.ses
  "C:\\Program Files\\KiCad\\10.0\\bin\\python.exe" neuroband_pcb.py main finish   # import routes, fill planes
  "C:\\Program Files\\KiCad\\10.0\\bin\\python.exe" neuroband_pcb.py main fab      # Gerbers, BOM, CPL, DRC, renders
(same for "ppg").  build.ps1 runs the whole chain.

Every connection is written by the symbol's pin NAME and resolved to a pad number
through KiCad's official symbol library (kicad_lib.py), so a typo fails loudly.
"""
import sys
from pathlib import Path

import pcbnew as pn

from kicad_lib import KICAD, pins

HERE = Path(__file__).resolve().parent
MM = pn.FromMM
X0, Y0 = 100.0, 100.0  # board origin on the KiCad sheet

R0402, C0402, C0603 = "Resistor_SMD:R_0402_1005Metric", "Capacitor_SMD:C_0402_1005Metric", "Capacitor_SMD:C_0603_1608Metric"
SOT23, SOT235, SOT236 = "Package_TO_SOT_SMD:SOT-23", "Package_TO_SOT_SMD:SOT-23-5", "Package_TO_SOT_SMD:SOT-23-6"
SH4 = "Connector_JST:JST_SH_SM04B-SRSS-TB_1x04-1MP_P1.00mm_Horizontal"
SH2 = "Connector_JST:JST_SH_SM02B-SRSS-TB_1x02-1MP_P1.00mm_Horizontal"
TP = "TestPoint:TestPoint_Pad_1.5x1.5mm"
QWIIC = {"1": "GND", "2": "3V3", "3": "I2C_SDA", "4": "I2C_SCL"}  # Qwiic / STEMMA QT pin order


def R(v, a, b, **kw):
    return dict(sym="Device:R", fp=R0402, val=v, mpn=f"{v} 1% 0402", conn={"1": a, "2": b}, **kw)


def C(v, a, b, fp=C0402, **kw):
    return dict(sym="Device:C", fp=fp, val=v, mpn=f"{v} X5R/X7R {fp.split('_')[-2]}", conn={"1": a, "2": b}, **kw)


# ---------------------------------------------------------------- main board
# at=(x, y, rot) fixes a part (mm from the board's top-left corner); near="REF" lets the
# placer find the closest free spot next to that part. side "B" = bottom of the board.
MAIN = dict(
    size=(34.0, 40.0), layers=4, thickness=1.0,
    # Espressif keep-out: no copper under the antenna and 5 mm to its right (board edge covers top/left)
    keepout=(0.0, 0.0, 24.5, 6.6),
    parts={
        # --- MCU
        "U1": dict(sym="RF_Module:ESP32-WROOM-32E", fp="RF_Module:ESP32-WROOM-32E", val="ESP32-WROOM-32E-N4",
                   mpn="ESP32-WROOM-32E-N4", at=(10.0, 12.8, 0), body=(-9.05, -12.8, 9.05, 12.8),
                   conn={"VDD": "3V3", "GND": "GND", "EN": "EN", "IO0": "IO0", "IO21": "I2C_SDA", "IO22": "I2C_SCL",
                         "IO34": "GSR_ADC", "IO35": "BATT_ADC", "IO27": "BTN", "IO25": "MOTOR",
                         "TXD0/IO1": "ESP_TX", "RXD0/IO3": "ESP_RX"}),
        "C2": C("10uF", "3V3", "GND", fp=C0603, near="U1", pin="2"),
        "C3": C("100nF", "3V3", "GND", near="U1", pin="2"),
        "R1": R("10k", "EN", "3V3", near="U1", pin="3", side="B"),
        "C1": C("1uF", "EN", "GND", near="U1", pin="3", side="B"),
        "R2": R("10k", "IO0", "3V3", near="U1", pin="25", side="B"),
        "SW1": dict(sym="Switch:SW_Push", fp="Button_Switch_SMD:SW_SPST_PTS810", val="RESET", mpn="PTS810 SJM 250 SMTR LFS",
                    conn={"1": "EN", "2": "GND"}, at=(11.0, 15.0, 0), side="B"),
        "SW2": dict(sym="Switch:SW_Push", fp="Button_Switch_SMD:SW_SPST_PTS810", val="BOOT", mpn="PTS810 SJM 250 SMTR LFS",
                    conn={"1": "IO0", "2": "GND"}, at=(11.0, 20.5, 0), side="B"),
        # --- USB-C, ESD, USB-UART, auto-reset
        "J1": dict(sym="Connector:USB_C_Receptacle_USB2.0_16P", fp="Connector_USB:USB_C_Receptacle_HRO_TYPE-C-31-M-12",
                   val="USB-C", mpn="TYPE-C-31-M-12", at=(8.0, 36.6, 0),
                   conn={"VBUS": "VBUS", "GND": "GND", "SHIELD": "GND", "CC1": "CC1", "CC2": "CC2",
                         "D+": "USB_DP", "D-": "USB_DM"}),
        "R5": R("5.1k", "CC1", "GND", near="J1", pin="A5"),
        "R6": R("5.1k", "CC2", "GND", near="J1", pin="B5"),
        "U3": dict(sym="Power_Protection:USBLC6-2SC6", fp=SOT236, val="USBLC6-2SC6", mpn="USBLC6-2SC6",
                   at=(8.0, 29.0, 0), conn={"I/O1": "USB_DP", "I/O2": "USB_DM", "VBUS": "VBUS", "GND": "GND"}),
        "U2": dict(sym="Interface_USB:CH340C", fp="Package_SO:SOIC-16_3.9x9.9mm_P1.27mm", val="CH340C", mpn="CH340C",
                   at=(19.5, 34.6, 90),
                   conn={"VCC": "3V3", "V3": "3V3", "GND": "GND", "UD+": "USB_DP", "UD-": "USB_DM",
                         "TXD": "ESP_RX", "RXD": "ESP_TX", "~{DTR}": "DTR", "~{RTS}": "RTS"}),
        "C4": C("100nF", "3V3", "GND", near="U2", pin="16"),
        "Q1": dict(sym="Transistor_BJT:MMBT3904", fp=SOT23, val="MMBT3904", mpn="MMBT3904",
                   conn={"B": "Q1_B", "E": "RTS", "C": "EN"}, near="U2", pin="14"),
        "Q2": dict(sym="Transistor_BJT:MMBT3904", fp=SOT23, val="MMBT3904", mpn="MMBT3904",
                   conn={"B": "Q2_B", "E": "DTR", "C": "IO0"}, near="U2", pin="13"),
        "R3": R("10k", "DTR", "Q1_B", near="Q1"),
        "R4": R("10k", "RTS", "Q2_B", near="Q2"),
        # --- battery, charger, power path, LDO, switch
        "TP3": dict(sym="Connector:TestPoint", fp=TP, val="BAT+", mpn="pad", conn={"1": "VBAT"}, at=(26.0, 31.0, 0), side="B"),
        "TP4": dict(sym="Connector:TestPoint", fp=TP, val="BAT-", mpn="pad", conn={"1": "GND"}, at=(29.0, 31.0, 0), side="B"),
        "U4": dict(sym="Battery_Management:MCP73831-2-OT", fp=SOT235, val="MCP73831", mpn="MCP73831T-2ACI/OT",
                   at=(22.0, 9.6, 0),
                   conn={"V_{DD}": "VBUS", "V_{BAT}": "VBAT", "V_{SS}": "GND", "PROG": "CHG_PROG", "STAT": "CHG_STAT"}),
        "R7": R("6.8k", "CHG_PROG", "GND", near="U4", pin="5"),   # ~150 mA charge
        "C5": C("4.7uF", "VBUS", "GND", fp=C0603, near="U4", pin="4"),
        "C6": C("4.7uF", "VBAT", "GND", fp=C0603, near="U4", pin="3"),
        "R8": R("1k", "VBUS", "LED_A", near="LED1", side="B"),
        "LED1": dict(sym="Device:LED", fp="LED_SMD:LED_0603_1608Metric", val="CHG", mpn="Red LED 0603",
                     conn={"A": "LED_A", "K": "CHG_STAT"}, near="U4", pin="1", side="B"),
        "D1": dict(sym="Device:D_Schottky", fp="Diode_SMD:D_SOD-123", val="B5819W", mpn="B5819W",
                   conn={"A": "VBUS", "K": "VSYS"}, at=(22.0, 18.6, 0)),
        "Q3": dict(sym="Transistor_FET:AO3401A", fp=SOT23, val="AO3401A", mpn="AO3401A",
                   conn={"G": "VBUS", "D": "VBAT", "S": "VSYS"}, at=(22.0, 22.0, 0)),
        "R9": R("100k", "VBUS", "GND", near="Q3", pin="1", side="B"),
        "U5": dict(sym="Regulator_Linear:AP2112K-3.3", fp=SOT235, val="AP2112K-3.3", mpn="AP2112K-3.3TRG1",
                   at=(22.0, 14.4, 0), conn={"VIN": "VSYS", "GND": "GND", "EN": "LDO_EN", "VOUT": "3V3"}),
        "C7": C("10uF", "VSYS", "GND", fp=C0603, near="U5", pin="1"),
        "C8": C("10uF", "3V3", "GND", fp=C0603, near="U5", pin="5"),
        # slide switch: common (B) drives LDO EN, A = on (VSYS), C = off (GND); carries no load current
        "SW4": dict(sym="Switch:SW_SPDT", fp="Button_Switch_SMD:SW_SPDT_PCM12", val="POWER", mpn="MSK-12C02 (PCM12 footprint)",
                    conn={"A": "VSYS", "B": "LDO_EN", "C": "GND"}, at=(31.3, 12.0, 90)),
        "R10": R("100k", "VBAT", "BATT_ADC", near="U1", pin="7", side="B"),
        "R11": R("100k", "BATT_ADC", "GND", near="U1", pin="7", side="B"),
        "C10": C("100nF", "BATT_ADC", "GND", near="U1", pin="7", side="B"),
        # --- I2C bus + sensors
        "R13": R("4.7k", "I2C_SDA", "3V3", near="U1", pin="33", side="B"),
        "R14": R("4.7k", "I2C_SCL", "3V3", near="U1", pin="36", side="B"),
        "U6": dict(sym="Sensor_Motion:MPU-6050", fp="Sensor_Motion:InvenSense_QFN-24_4x4mm_P0.5mm", val="MPU-6050",
                   mpn="MPU-6050", at=(27.0, 23.5, 0),
                   # AD0 high -> I2C address 0x69 (firmware scans 0x68 and 0x69); it shares the VLOGIC pin next to it
                   conn={"VDD": "3V3", "VLOGIC": "3V3", "GND": "GND", "AD0": "3V3", "CLKIN": "GND", "FSYNC": "GND",
                         "REGOUT": "MPU_REG", "CPOUT": "MPU_CP", "SDA": "I2C_SDA", "SCL": "I2C_SCL"}),
        "C11": C("100nF", "3V3", "GND", near="U6", pin="13"),
        "C12": C("10nF", "3V3", "GND", near="U6", pin="8"),
        "C13": C("100nF", "MPU_REG", "GND", near="U6", pin="10"),
        "C14": C("2.2nF", "MPU_CP", "GND", near="U6", pin="20"),
        "J4": dict(sym="Connector_Generic:Conn_01x04", fp=SH4, val="OLED", mpn="SM04B-SRSS-TB", conn=QWIIC,
                   at=(29.5, 3.6, 180)),
        "J5": dict(sym="Connector_Generic:Conn_01x04", fp=SH4, val="PPG", mpn="SM04B-SRSS-TB", conn=QWIIC,
                   at=(3.4, 10.0, 90), side="B"),
        "J6": dict(sym="Connector_Generic:Conn_01x04", fp=SH4, val="TEMP", mpn="SM04B-SRSS-TB", conn=QWIIC,
                   at=(3.4, 19.0, 90), side="B"),
        # --- GSR front-end
        "J3": dict(sym="Connector_Generic:Conn_01x02", fp=SH2, val="GSR", mpn="SM02B-SRSS-TB",
                   conn={"1": "GSR_A", "2": "GSR_X"}, at=(3.4, 28.0, 90), side="B"),
        "R15": R("10k", "3V3", "GSR_A", near="J3", pin="1", side="B"),
        "D3": dict(sym="Device:D_TVS", fp="Diode_SMD:D_SOD-323", val="PESD5V0S1BL", mpn="PESD5V0S1BL",
                   conn={"1": "GSR_A", "2": "GND"}, near="J3", pin="1", side="B"),
        "D4": dict(sym="Device:D_TVS", fp="Diode_SMD:D_SOD-323", val="PESD5V0S1BL", mpn="PESD5V0S1BL",
                   conn={"1": "GSR_X", "2": "GND"}, near="J3", pin="2", side="B"),
        "R16": R("100k", "GSR_X", "GND", near="J3", pin="2", side="B"),
        "U7": dict(sym="Amplifier_Operational:MCP6001-OT", fp=SOT235, val="MCP6001", mpn="MCP6001T-I/OT",
                   conn={"+": "GSR_X", "-": "GSR_BUF", "1": "GSR_BUF", "V+": "3V3", "V-": "GND"},
                   at=(10.5, 27.6, 0), side="B"),
        "C15": C("100nF", "3V3", "GND", near="U7", pin="5", side="B"),
        "R17": R("10k", "GSR_BUF", "GSR_ADC", near="U7", pin="1", side="B"),
        "C16": C("1uF", "GSR_ADC", "GND", near="U7", pin="1", side="B"),
        # --- side button, vibration motor
        "SW3": dict(sym="Switch:SW_Push", fp="Button_Switch_SMD:SW_Push_1P1T-MP_NO_Horizontal_Alps_SKRTLAE010",
                    val="BTN", mpn="SKRTLAE010", conn={"1": "BTN", "2": "GND"}, at=(32.0, 20.5, 90)),
        "TP1": dict(sym="Connector:TestPoint", fp=TP, val="MOT+", mpn="pad", conn={"1": "VSYS"}, at=(29.0, 36.0, 0), side="B"),
        "TP2": dict(sym="Connector:TestPoint", fp=TP, val="MOT-", mpn="pad", conn={"1": "MOTOR_N"}, at=(26.0, 36.0, 0), side="B"),
        "Q4": dict(sym="Transistor_FET:AO3400A", fp=SOT23, val="AO3400A", mpn="AO3400A",
                   conn={"G": "MOTOR_G", "D": "MOTOR_N", "S": "GND"}, near="TP2", side="B"),
        "R18": R("100", "MOTOR", "MOTOR_G", near="Q4", side="B"),
        "R19": R("100k", "MOTOR_G", "GND", near="Q4", side="B"),
        "D2": dict(sym="Device:D", fp="Diode_SMD:D_SOD-323", val="1N4148WS", mpn="1N4148WS",
                   conn={"A": "MOTOR_N", "K": "VSYS"}, at=(27.5, 38.3, 0), side="B"),
    },
    labels=[("NeuroBand AI v1", 17.0, 39.0, "F"), ("BAT+", 26.0, 29.2, "B"), ("BAT-", 29.0, 29.2, "B"),
            ("MOT-", 26.0, 33.6, "B"), ("MOT+", 29.0, 33.6, "B")],
    power=["VBUS", "VSYS", "VBAT", "3V3", "GND"],
    planes=[("In1.Cu", "GND"), ("In2.Cu", "3V3"), ("F.Cu", "GND"), ("B.Cu", "GND")],
)

# ---------------------------------------------------------------- PPG sensor board (bottom shell)
PPG = dict(
    size=(12.0, 15.0), layers=2, thickness=0.8, keepout=None,
    parts={
        "U1": dict(sym="Sensor:MAX30102", fp="OptoDevice:Maxim_OLGA-14_3.3x5.6mm_P0.8mm", val="MAX30102",
                   mpn="MAX30102EFD+T", at=(6.0, 6.0, 0), side="B",
                   conn={"VDD": "1V8", "GND": "GND", "PGND": "GND", "VLED+": "3V3", "SCL": "I2C_SCL", "SDA": "I2C_SDA"}),
        "U2": dict(sym="Regulator_Linear:XC6206PxxxMR", fp="Package_TO_SOT_SMD:SOT-23-3", val="XC6206P182MR",
                   mpn="XC6206P182MR", at=(3.5, 6.0, 0), conn={"VI": "3V3", "VO": "1V8", "GND": "GND"}),
        "C1": C("1uF", "3V3", "GND", near="U2", pin="3"),
        "C2": C("1uF", "1V8", "GND", near="U2", pin="2"),
        "C3": C("100nF", "1V8", "GND", near="U1", pin="11", side="B"),
        "C4": C("10uF", "3V3", "GND", fp=C0603, near="U1", pin="9", side="B"),
        "C5": C("100nF", "3V3", "GND", near="U1", pin="10", side="B"),
        "J1": dict(sym="Connector_Generic:Conn_01x04", fp=SH4, val="MAIN", mpn="SM04B-SRSS-TB", conn=QWIIC,
                   at=(6.0, 11.6, 0)),
    },
    labels=[("PPG", 9.5, 3.0, "F")],
    power=["3V3", "1V8", "GND"],
    planes=[("F.Cu", "GND"), ("B.Cu", "GND")],
)

BOARDS = {"main": MAIN, "ppg": PPG}


# ---------------------------------------------------------------- building blocks
def vec(x, y):
    return pn.VECTOR2I(MM(X0 + x), MM(Y0 + y))


def load_fp(fp_id):
    lib, name = fp_id.split(":")
    return pn.FootprintLoad(str(KICAD / "footprints" / f"{lib}.pretty"), name)


def outline(board, w, h, r=2.0):
    def seg(a, b):
        s = pn.PCB_SHAPE(board, pn.SHAPE_T_SEGMENT)
        s.SetStart(vec(*a)); s.SetEnd(vec(*b)); s.SetLayer(pn.Edge_Cuts); s.SetWidth(MM(0.1)); board.Add(s)

    def arc(start, mid, end):
        s = pn.PCB_SHAPE(board, pn.SHAPE_T_ARC)
        s.SetArcGeometry(vec(*start), vec(*mid), vec(*end)); s.SetLayer(pn.Edge_Cuts); s.SetWidth(MM(0.1)); board.Add(s)

    k = r * (1 - 0.7071)
    seg((r, 0), (w - r, 0)); seg((w, r), (w, h - r)); seg((w - r, h), (r, h)); seg((0, h - r), (0, r))
    arc((w - r, 0), (w - k, k), (w, r)); arc((w, h - r), (w - k, h - k), (w - r, h))
    arc((r, h), (k, h - k), (0, h - r)); arc((0, r), (k, k), (r, 0))


def rect_poly(x1, y1, x2, y2):
    p = pn.SHAPE_LINE_CHAIN()
    for x, y in ((x1, y1), (x2, y1), (x2, y2), (x1, y2)):
        p.Append(vec(x, y))
    p.SetClosed(True)
    return p


def bbox_mm(fp, side):
    fp.BuildCourtyardCaches()
    c = fp.GetCourtyard(pn.B_CrtYd if side == "B" else pn.F_CrtYd)
    b = c.BBox() if c.OutlineCount() else fp.GetBoundingBox(False)
    return (pn.ToMM(b.GetLeft()) - X0, pn.ToMM(b.GetTop()) - Y0, pn.ToMM(b.GetRight()) - X0, pn.ToMM(b.GetBottom()) - Y0)


def overlaps(a, b, gap=0.05):
    return not (a[2] + gap <= b[0] or b[2] + gap <= a[0] or a[3] + gap <= b[1] or b[3] + gap <= a[1])


def put(fp, x, y, rot, side):
    fp.SetPosition(vec(x, y))
    if side == "B" and not fp.IsFlipped():
        fp.Flip(fp.GetPosition(), pn.FLIP_DIRECTION_LEFT_RIGHT)
    fp.SetOrientationDegrees(rot)


def pad_pos(fp, num):
    p = next(p for p in fp.Pads() if p.GetNumber() == num)
    return pn.ToMM(p.GetPosition().x) - X0, pn.ToMM(p.GetPosition().y) - Y0


def fanout(board, plane_nets, w, h, keepout):
    """Short track + via from every SMD pad on a plane net down to its inner plane.
    The autorouter treats plane nets as already connected, so it never adds these itself."""
    import math
    VIA_R, CLR = 0.3, 0.15

    def mm_rect(item):
        b = item.GetBoundingBox()
        return pn.ToMM(b.GetLeft()) - X0, pn.ToMM(b.GetTop()) - Y0, pn.ToMM(b.GetRight()) - X0, pn.ToMM(b.GetBottom()) - Y0

    def cu(pd):  # GetPrincipalLayer() reports F.Cu even for bottom pads in KiCad 10
        return pn.B_Cu if pd.IsOnLayer(pn.B_Cu) and not pd.IsOnLayer(pn.F_Cu) else pn.F_Cu

    def dist_rect(x, y, r):
        dx = max(r[0] - x, 0, x - r[2]); dy = max(r[1] - y, 0, y - r[3])
        return math.hypot(dx, dy)

    pads = [(pd, mm_rect(pd)) for fp in board.GetFootprints() for pd in fp.Pads()]
    vias, segs, made, failed = [], [], 0, []  # vias: (x, y, net), segs: (x1, y1, x2, y2, width, net)

    def seg_dist(px, py, s):
        x1, y1, x2, y2 = s[:4]
        L2 = (x2 - x1) ** 2 + (y2 - y1) ** 2 or 1e-9
        t = max(0, min(1, ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / L2))
        return math.hypot(px - (x1 + t * (x2 - x1)), py - (y1 + t * (y2 - y1)))

    def ok(net, cx, cy, sx, sy, tw, own, reuse=False):
        if not (0.7 < cx < w - 0.7 and 0.7 < cy < h - 0.7):
            return False
        if keepout and dist_rect(cx, cy, keepout) < VIA_R + 0.3:
            return False
        for pd, r in pads:
            other = pd.GetNetname() != net or pd.GetAttribute() == pn.PAD_ATTRIB_NPTH
            if other and dist_rect(cx, cy, r) < VIA_R + CLR + 0.05:
                return False
            if other and pd is not own and pd.IsOnLayer(cu(own)):
                n = max(2, int(math.hypot(cx - sx, cy - sy) / 0.05))
                if any(dist_rect(sx + (cx - sx) * k / n, sy + (cy - sy) * k / n, r) < tw / 2 + CLR + 0.02 for k in range(n + 1)):
                    return False
        for vx, vy, vn in vias:
            if reuse and (vx, vy) == (cx, cy):
                continue
            if math.hypot(cx - vx, cy - vy) < 2 * VIA_R + CLR + 0.05:
                return False
            if vn != net and seg_dist(vx, vy, (sx, sy, cx, cy)) < VIA_R + tw / 2 + CLR:
                return False
        for s in segs:
            if s[5] != net and (seg_dist(cx, cy, s) < VIA_R + s[4] / 2 + CLR or
                                min(seg_dist(sx + (cx - sx) * k / 8, sy + (cy - sy) * k / 8, s) for k in range(9)) < tw / 2 + s[4] / 2 + CLR):
                return False
        return True

    for fp in board.GetFootprints():
        fcx, fcy = pn.ToMM(fp.GetPosition().x) - X0, pn.ToMM(fp.GetPosition().y) - Y0
        for pd in fp.Pads():
            net = pd.GetNetname()
            if net not in plane_nets or pd.GetAttribute() != pn.PAD_ATTRIB_SMD:
                continue
            r = mm_rect(pd)
            sx, sy = (r[0] + r[2]) / 2, (r[1] + r[3]) / 2
            pw, ph = r[2] - r[0], r[3] - r[1]
            if pw > 2.0 and ph > 2.0:  # big thermal pad: vias straight into it
                for vx, vy in ((sx - pw / 4, sy - ph / 4), (sx + pw / 4, sy - ph / 4), (sx - pw / 4, sy + ph / 4), (sx + pw / 4, sy + ph / 4)):
                    vias.append((vx, vy, net))
                    v = pn.PCB_VIA(board); v.SetPosition(vec(vx, vy)); v.SetWidth(MM(0.6)); v.SetDrill(MM(0.3))
                    v.SetNet(pd.GetNet()); board.Add(v); made += 1
                continue
            tw = max(0.2, min(0.4, min(pw, ph)))
            base = math.atan2(sy - fcy, sx - fcx) if math.hypot(sx - fcx, sy - fcy) > 0.05 else 0.0
            done = False
            # a same-net via close by? just run a track to it
            for vx, vy, vn in sorted(vias, key=lambda v: math.hypot(v[0] - sx, v[1] - sy)):
                if vn == net and math.hypot(vx - sx, vy - sy) < 1.6 and ok(net, vx, vy, sx, sy, tw, pd, reuse=True):
                    t = pn.PCB_TRACK(board); t.SetStart(vec(sx, sy)); t.SetEnd(vec(vx, vy)); t.SetWidth(MM(tw))
                    t.SetLayer(cu(pd)); t.SetNet(pd.GetNet()); board.Add(t)
                    segs.append((sx, sy, vx, vy, tw, net)); made += 1; done = True
                    break
            for dd in ([] if done else [i * 0.1 for i in range(0, 30)]):
                for off in (0, 20, -20, 40, -40, 60, -60, 90, -90, 120, -120, 150, -150, 180):
                    a = base + math.radians(off)
                    reach = abs(math.cos(a)) * pw / 2 + abs(math.sin(a)) * ph / 2
                    cx, cy = sx + math.cos(a) * (reach + VIA_R + CLR + dd), sy + math.sin(a) * (reach + VIA_R + CLR + dd)
                    if ok(net, cx, cy, sx, sy, tw, pd):
                        t = pn.PCB_TRACK(board); t.SetStart(vec(sx, sy)); t.SetEnd(vec(cx, cy)); t.SetWidth(MM(tw))
                        t.SetLayer(cu(pd)); t.SetNet(pd.GetNet()); board.Add(t)
                        v = pn.PCB_VIA(board); v.SetPosition(vec(cx, cy)); v.SetWidth(MM(0.6)); v.SetDrill(MM(0.3))
                        v.SetNet(pd.GetNet()); board.Add(v)
                        vias.append((cx, cy, net)); segs.append((sx, sy, cx, cy, tw, net)); made += 1
                        done = True
                        break
                if done:
                    break
            if not done:
                failed.append(f"{fp.GetReference()}.{pd.GetNumber()}")
    print(f"fanout: {made} plane vias" + (f", no room at {failed}" if failed else ""))


def build(name):
    spec = BOARDS[name]
    out = HERE / name
    out.mkdir(exist_ok=True)
    path = out / f"neuroband_{name}.kicad_pcb"
    board = pn.NewBoard(str(path))
    w, h = spec["size"]
    board.SetCopperLayerCount(spec["layers"])
    ds = board.GetDesignSettings()
    ds.SetBoardThickness(MM(spec["thickness"]))
    ds.m_TrackMinWidth, ds.m_MinClearance, ds.m_CopperEdgeClearance = MM(0.127), MM(0.127), MM(0.3)
    ds.m_ViasMinSize, ds.m_MinThroughDrill, ds.m_HoleClearance = MM(0.45), MM(0.3), MM(0.2)
    ns = ds.m_NetSettings
    dflt = ns.GetDefaultNetclass()
    dflt.SetClearance(MM(0.15)); dflt.SetTrackWidth(MM(0.2)); dflt.SetViaDiameter(MM(0.6)); dflt.SetViaDrill(MM(0.3))
    pwr = pn.NETCLASS("Power")
    pwr.SetClearance(MM(0.15)); pwr.SetTrackWidth(MM(0.4)); pwr.SetViaDiameter(MM(0.6)); pwr.SetViaDrill(MM(0.3))
    ns.SetNetclass("Power", pwr)
    for n in spec["power"]:
        ns.SetNetclassPatternAssignment(n, "Power")
    outline(board, w, h)
    tb = board.GetTitleBlock()
    tb.SetTitle(f"NeuroBand AI - {'main board' if name == 'main' else 'PPG sensor board'}")
    tb.SetRevision("v1"); tb.SetComment(0, "Generated by hardware/neuroband_pcb.py from PCB_DESIGN_SPEC.md")

    nets = {}

    def net(n):
        if n not in nets:
            nets[n] = pn.NETINFO_ITEM(board, n)
            board.Add(nets[n])
        return nets[n]

    used = {"F": [], "B": []}
    if spec["keepout"]:
        used["F"].append(spec["keepout"]); used["B"].append(spec["keepout"])
    placed = {}
    todo = list(spec["parts"].items())
    fixed = [kv for kv in todo if "at" in kv[1]]
    floating, done = [], {r for r, _ in fixed}
    pending = [kv for kv in todo if "at" not in kv[1]]
    while pending:  # a part goes after the part it sits next to
        ready = [kv for kv in pending if kv[1]["near"] in done]
        floating += ready; done |= {r for r, _ in ready}
        pending = [kv for kv in pending if kv not in ready]
    for ref, p in fixed + floating:
        fp = load_fp(p["fp"])
        for z in list(fp.Zones()):  # library keep-outs (ESP32's 48 mm one): we draw our own
            fp.Remove(z)
        fp.SetReference(ref); fp.SetValue(p["val"])
        if ref == "U1" and name == "main":
            # drop the module's 0.2 mm thermal-via pads (off JLC's standard drill range) and its
            # 48 mm wide antenna courtyard; the antenna keep-out zone below does that job
            for pd in [pd for pd in fp.Pads() if pd.GetAttribute() == pn.PAD_ATTRIB_PTH]:
                fp.Remove(pd)
            for g in [g for g in fp.GraphicalItems() if g.GetLayer() == pn.F_CrtYd]:
                fp.Remove(g)
            cy = pn.PCB_SHAPE(fp, pn.SHAPE_T_RECT)
            cy.SetStart(pn.VECTOR2I(MM(-9.3), MM(-13.05))); cy.SetEnd(pn.VECTOR2I(MM(9.3), MM(13.05)))
            cy.SetLayer(pn.F_CrtYd); cy.SetWidth(MM(0.05)); fp.Add(cy)
        board.Add(fp)  # Flip() needs the footprint on a board
        side = p.get("side", "F")
        if "at" in p:
            x, y, rot = p["at"]
            put(fp, x, y, rot, side)
            bb = bbox_mm(fp, side)
            if "body" in p:  # module: occupy its body, not the huge antenna courtyard
                bx1, by1, bx2, by2 = p["body"]
                bb = (x + bx1, y + by1, x + bx2, y + by2)
            if any(overlaps(bb, u, 0) for u in used[side][1 if spec["keepout"] else 0:]):
                print(f"WARNING: {ref} overlaps another part")
        else:
            ax, ay = pad_pos(placed[p["near"]], p["pin"]) if "pin" in p else \
                (pn.ToMM(placed[p["near"]].GetPosition().x) - X0, pn.ToMM(placed[p["near"]].GetPosition().y) - Y0)
            bb = None
            shapes = {}
            for rot in (0, 90):  # courtyard at the origin, then just shift it around
                put(fp, 0, 0, rot, side)
                shapes[rot] = bbox_mm(fp, side)
            grid = sorted(((i * 0.25, j * 0.25) for i in range(int(w * 4) + 1) for j in range(int(h * 4) + 1)),
                          key=lambda q: (q[0] - ax) ** 2 + (q[1] - ay) ** 2)
            for x, y in grid:
                for rot, s0 in shapes.items():
                    b = (s0[0] + x, s0[1] + y, s0[2] + x, s0[3] + y)
                    if b[0] < 0.2 or b[1] < 0.2 or b[2] > w - 0.2 or b[3] > h - 0.2:
                        continue
                    if any(overlaps(b, u, 0.35) for u in used[side]):  # room for vias + routing
                        continue
                    bb = b
                    put(fp, x, y, rot, side)
                    break
                if bb:
                    break
            if not bb:
                raise SystemExit(f"no room for {ref} near {p['near']}")
        through = any(pd.GetAttribute() in (pn.PAD_ATTRIB_PTH, pn.PAD_ATTRIB_NPTH) for pd in fp.Pads())
        for s in (("F", "B") if through else (side,)):
            used[s].append(bb)
        lib_pins = pins(p["sym"])
        for pin, n in p["conn"].items():
            nums = lib_pins.get(pin)
            if not nums:
                raise SystemExit(f"{ref}: symbol {p['sym']} has no pin '{pin}'")
            for num in nums:
                pads = [pd for pd in fp.Pads() if pd.GetNumber() == num]
                if not pads:
                    raise SystemExit(f"{ref}: footprint {p['fp']} has no pad '{num}'")
                for pd in pads:
                    pd.SetNet(net(n))
        t = fp.Reference()  # silk labels only on parts big enough to carry them
        t.SetTextSize(pn.VECTOR2I(MM(0.8), MM(0.8))); t.SetTextThickness(MM(0.12))
        t.SetVisible(ref[0] in "UJS" or ref.startswith("TP"))
        placed[ref] = fp

    for text, x, y, side in spec["labels"]:
        t = pn.PCB_TEXT(board)
        t.SetText(text); t.SetPosition(vec(x, y)); t.SetLayer(pn.B_SilkS if side == "B" else pn.F_SilkS)
        t.SetTextSize(pn.VECTOR2I(MM(0.8), MM(0.8))); t.SetTextThickness(MM(0.12))
        if side == "B":
            t.SetMirrored(True)
        board.Add(t)

    if spec["keepout"]:
        z = pn.ZONE(board)
        z.SetIsRuleArea(True)
        z.SetDoNotAllowTracks(True); z.SetDoNotAllowVias(True); z.SetDoNotAllowPads(True); z.SetDoNotAllowZoneFills(True)
        z.SetDoNotAllowFootprints(False)
        ls = pn.LSET.AllCuMask(spec["layers"])
        z.SetLayerSet(ls)
        z.Outline().AddOutline(rect_poly(*spec["keepout"]))
        z.SetZoneName("ESP32 antenna keep-out")
        board.Add(z)

    # 0.35 mm no-track/no-via ring along the edge: the autorouter ignores KiCad's edge clearance
    for x1, y1, x2, y2 in ((0, 0, w, 0.35), (0, h - 0.35, w, h), (0, 0, 0.35, h), (w - 0.35, 0, w, h)):
        z = pn.ZONE(board)
        z.SetIsRuleArea(True)
        z.SetDoNotAllowTracks(True); z.SetDoNotAllowVias(True)
        z.SetDoNotAllowPads(False); z.SetDoNotAllowZoneFills(False); z.SetDoNotAllowFootprints(False)
        z.SetLayerSet(pn.LSET.AllCuMask(spec["layers"]))
        z.Outline().AddOutline(rect_poly(x1, y1, x2, y2))
        board.Add(z)

    def pour(layer, n):
        z = pn.ZONE(board)
        z.SetLayer(board.GetLayerID(layer)); z.SetNet(net(n))
        z.Outline().AddOutline(rect_poly(0, 0, w, h))
        z.SetLocalClearance(MM(0.2)); z.SetMinThickness(MM(0.2))
        z.SetPadConnection(pn.ZONE_CONNECTION_THERMAL if layer.startswith("In") else pn.ZONE_CONNECTION_FULL)
        z.SetThermalReliefGap(MM(0.2)); z.SetThermalReliefSpokeWidth(MM(0.3))
        board.Add(z)

    # inner layers are solid planes; pads reach them through fanout vias, the router never routes there
    inner = {n for layer, n in spec["planes"] if layer.startswith("In")}
    for layer, n in spec["planes"]:
        if layer.startswith("In"):
            pour(layer, n)
    if inner:
        fanout(board, inner, w, h, spec["keepout"])
    dsn = out / f"neuroband_{name}.dsn"
    if not pn.ExportSpecctraDSN(board, str(dsn)):
        raise SystemExit("DSN export failed")
    text = dsn.read_text()
    for layer, _ in spec["planes"]:
        if layer.startswith("In"):
            text = text.replace(f"(layer {layer}\n      (type signal)", f"(layer {layer}\n      (type power)")
    dsn.write_text(text)
    # outer GND pours go in after routing (they fill around the tracks)
    for layer, n in spec["planes"]:
        if not layer.startswith("In"):
            pour(layer, n)
    pn.ZONE_FILLER(board).Fill(board.Zones())
    pn.SaveBoard(str(path), board)
    print(f"placed {len(placed)} parts, {len(nets)} nets -> {path.name}, {dsn.name}")
    for s in "FB":
        print(s, "side parts:", sorted(r for r, p in spec["parts"].items() if p.get("side", "F") == s))


def finish(name):
    out = HERE / name
    path = out / f"neuroband_{name}.kicad_pcb"
    board = pn.LoadBoard(str(path))
    ses = out / f"neuroband_{name}.ses"
    if not pn.ImportSpecctraSES(board, str(ses)):
        raise SystemExit("SES import failed")
    pn.ZONE_FILLER(board).Fill(board.Zones())
    pn.SaveBoard(str(path), board)
    print("routes imported, zones filled ->", path.name)


def fab(name):
    """Gerber zip + drill, JLCPCB BOM and CPL, DRC report and 3D renders, into <name>/fab."""
    import csv, shutil, subprocess
    cli = str(KICAD.parent.parent / "bin" / "kicad-cli.exe")
    out = HERE / name
    pcb = str(out / f"neuroband_{name}.kicad_pcb")
    fabdir = out / "fab"
    shutil.rmtree(fabdir, ignore_errors=True)
    gdir = fabdir / "gerbers"
    gdir.mkdir(parents=True)
    cu = "F.Cu,In1.Cu,In2.Cu,B.Cu" if BOARDS[name]["layers"] == 4 else "F.Cu,B.Cu"
    run = lambda *a: subprocess.run([cli, *a], check=True, capture_output=True, text=True)
    run("pcb", "export", "gerbers", "--layers", cu + ",F.Paste,B.Paste,F.Silkscreen,B.Silkscreen,F.Mask,B.Mask,Edge.Cuts",
        "--no-protel-ext", "-o", str(gdir), pcb)
    run("pcb", "export", "drill", "--format", "excellon", "--drill-origin", "absolute", "--excellon-units", "mm",
        "--generate-map", "--map-format", "gerberx2", "-o", str(gdir) + "/", pcb)
    shutil.make_archive(str(fabdir / f"neuroband_{name}_gerbers"), "zip", gdir)
    run("pcb", "drc", "--severity-all", "-o", str(fabdir / "drc.rpt"), pcb)
    for side in ("top", "bottom"):
        run("pcb", "render", "--side", side, "--width", "1400", "--height", "1600", "--background", "opaque",
            "-o", str(fabdir / f"render_{side}.png"), pcb)

    parts = BOARDS[name]["parts"]
    real = {r: p for r, p in parts.items() if not r.startswith("TP")}  # pads are copper, not parts
    groups = {}
    for ref, p in real.items():
        groups.setdefault((p["mpn"], p["fp"].split(":")[1]), []).append(ref)
    with open(fabdir / f"neuroband_{name}_BOM_JLCPCB.csv", "w", newline="") as f:
        wr = csv.writer(f)
        wr.writerow(["Comment", "Designator", "Footprint", "LCSC Part #"])
        for (mpn, fpn), refs in sorted(groups.items(), key=lambda g: g[1][0]):
            wr.writerow([mpn, ",".join(sorted(refs)), fpn, ""])
    pos = fabdir / "pos.csv"
    run("pcb", "export", "pos", "--format", "csv", "--units", "mm", "--side", "both", "--use-drill-file-origin",
        "-o", str(pos), pcb)
    # same coordinate frame as the Gerbers, so JLCPCB's placement preview lines up
    with open(pos) as src, open(fabdir / f"neuroband_{name}_CPL_JLCPCB.csv", "w", newline="") as f:
        wr = csv.writer(f)
        wr.writerow(["Designator", "Mid X", "Mid Y", "Layer", "Rotation"])
        for row in csv.DictReader(src):
            if row["Ref"] in real:
                wr.writerow([row["Ref"], row["PosX"], row["PosY"], row["Side"].capitalize(), row["Rot"]])
    pos.unlink()
    print(f"fab files -> {fabdir}")


if __name__ == "__main__":
    name, stage = sys.argv[1], sys.argv[2]
    {"place": build, "finish": finish, "fab": fab}[stage](name)
