#!/usr/bin/env python3
"""Générateur de clés de licence Echeo, sans serveur.

Installation (une fois) :  pip install cryptography

  python echeo_licence.py keygen
      Crée la paire de clés (une seule fois). La clé privée reste sur ton PC,
      dans tools/licence/keys/. Affiche la clé publique à donner au projet.

  python echeo_licence.py issue ECH-XXXX-XXXX
      Génère une clé pour le téléphone dont l'ID est ECH-XXXX-XXXX.
      La clé est valable 1 mois à partir d'aujourd'hui. Chaque clé émise
      est aussi notée dans keys/historique.txt pour suivre les copies.
"""
import base64
import calendar
import datetime
import json
import re
import sys
from pathlib import Path

from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import ec

KEY_DIR = Path(__file__).resolve().parent / "keys"
PRIVATE = KEY_DIR / "private.pem"
HISTORY = KEY_DIR / "historique.txt"
PREFIX = "ECH1"
VALID_MONTHS = 1
DEVICE_RE = re.compile(r"ECH-[0-9A-F]{4}-[0-9A-F]{4}")


def b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode()


def add_months(day: datetime.date, months: int) -> datetime.date:
    month = day.month - 1 + months
    year = day.year + month // 12
    month = month % 12 + 1
    last = calendar.monthrange(year, month)[1]
    return datetime.date(year, month, min(day.day, last))


def keygen() -> None:
    if PRIVATE.exists():
        sys.exit("Une clé privée existe déjà. Ne la remplace pas : toutes les clés émises deviendraient invalides.")
    KEY_DIR.mkdir(parents=True, exist_ok=True)
    key = ec.generate_private_key(ec.SECP256R1())
    PRIVATE.write_bytes(
        key.private_bytes(
            serialization.Encoding.PEM,
            serialization.PrivateFormat.PKCS8,
            serialization.NoEncryption(),
        )
    )
    public = key.public_key().public_bytes(
        serialization.Encoding.DER,
        serialization.PublicFormat.SubjectPublicKeyInfo,
    )
    print("Clé privée créée : " + str(PRIVATE))
    print("Clé publique (à donner pour LicenceConfig.PUBLIC_KEY) :")
    print(base64.b64encode(public).decode())


def issue(device_id: str) -> None:
    if not PRIVATE.exists():
        sys.exit("Lance d'abord : python echeo_licence.py keygen")
    device_id = device_id.strip().upper()
    if not DEVICE_RE.fullmatch(device_id):
        sys.exit("ID invalide. Format attendu : ECH-XXXX-XXXX (affiché dans l'app, Paramètres).")

    private = serialization.load_pem_private_key(PRIVATE.read_bytes(), password=None)
    today = datetime.date.today()
    payload = json.dumps(
        {"d": device_id, "i": (today - datetime.date(1970, 1, 1)).days},
        separators=(",", ":"),
    ).encode()
    signature = private.sign(payload, ec.ECDSA(hashes.SHA256()))
    key = f"{PREFIX}.{b64url(payload)}.{b64url(signature)}"

    expiry = add_months(today, VALID_MONTHS)
    with HISTORY.open("a", encoding="utf-8") as f:
        f.write(f"{today.isoformat()}\t{device_id}\texpire {expiry.isoformat()}\n")

    print(key)
    print(f"Valable jusqu'au {expiry.strftime('%d/%m/%Y')}.")


def main() -> None:
    args = sys.argv[1:]
    if args == ["keygen"]:
        keygen()
    elif len(args) == 2 and args[0] == "issue":
        issue(args[1])
    else:
        print(__doc__)


if __name__ == "__main__":
    main()
