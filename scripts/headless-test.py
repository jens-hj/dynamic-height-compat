#!/usr/bin/env python3
"""Run graphical Minecraft tests on an isolated display, without desktop popups/input."""
import json
import os
from pathlib import Path
import shutil
import signal
import subprocess
import sys
import time

root = Path(__file__).resolve().parents[1]
xvfb = shutil.which("Xvfb")
if xvfb is None:
    candidates = sorted(Path("/nix/store").glob("*-xorg-server-*/bin/Xvfb"))
    xvfb = str(candidates[-1]) if candidates else None
if not xvfb:
    raise SystemExit("Xvfb is required; run inside nix develop")
if len(sys.argv) < 2:
    raise SystemExit("Usage: headless-test.py ./gradlew :neoforge:runClientTest [options]")
started = time.time()
with (root / "build" / "xvfb.log").open("w") as log:
    display = subprocess.Popen([xvfb, "-displayfd", "1", "-screen", "0", "1280x720x24", "-nolisten", "tcp"],
                               stdout=subprocess.PIPE, stderr=log, text=True)
    try:
        number = display.stdout.readline().strip()
        if not number.isdigit():
            raise SystemExit("Could not start isolated X display; see build/xvfb.log")
        environment = os.environ.copy()
        environment["DISPLAY"] = ":" + number
        environment["SDL_VIDEO_DRIVER"] = "x11"
        environment.pop("WAYLAND_DISPLAY", None)
        # Runtime tests must inherit the isolated display, even when a Gradle daemon exists.
        command = sys.argv[1:]
        if "gradlew" in Path(command[0]).name and "--no-daemon" not in command:
            command.append("--no-daemon")
        process = subprocess.Popen(command, env=environment, cwd=root, start_new_session=True)
        try:
            code = process.wait(timeout=300)
        except subprocess.TimeoutExpired:
            # This isolated process group belongs only to this test invocation.
            os.killpg(process.pid, signal.SIGTERM)
            process.wait(timeout=15)
            raise SystemExit("Client regression exceeded five minutes")
        if code:
            raise SystemExit(code)
        marker = root / "build/client-result.json"
        if not marker.exists() or marker.stat().st_mtime < started:
            raise SystemExit("Minecraft exited without completing the graphical test")
        result = json.loads(marker.read_text())
        print(result["message"])
        if not result["passed"]:
            raise SystemExit("Client assertions did not pass")
    finally:
        display.terminate()
        display.wait(timeout=10)
