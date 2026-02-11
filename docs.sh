#!/usr/bin/env bash
# ═══════════════════════════════════════════════════════════════════════════════
#  docs.sh — Wixy Documentation (Docusaurus)
# ═══════════════════════════════════════════════════════════════════════════════
#
#  Usage:
#    ./docs.sh                  # Install deps + dev server on http://localhost:3000
#    ./docs.sh serve            # Install deps + dev server on http://localhost:3000
#    ./docs.sh build            # Build static site to ./documentation/build
#    ./docs.sh preview          # Build + serve production build on http://localhost:3000
#    ./docs.sh stop             # Stop the running docs server
#    ./docs.sh clean            # Clear Docusaurus cache + build artifacts
#    ./docs.sh status           # Show whether the server is running
#    ./docs.sh help             # Show this help message
#
# ═══════════════════════════════════════════════════════════════════════════════

set -euo pipefail

# ─── Configuration ─────────────────────────────────────────────────────────────
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCS_DIR="${ROOT_DIR}/documentation"
PID_FILE="${ROOT_DIR}/.docs-server.pid"
LOG_FILE="${ROOT_DIR}/.docs-server.log"
ADDR_FILE="${ROOT_DIR}/.docs-server.addr"

HOST="${HOST:-127.0.0.1}"
PORT="${PORT:-3000}"

# ─── ANSI Colors ───────────────────────────────────────────────────────────────
BOLD='\033[1m'
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
DIM='\033[2m'
NC='\033[0m'

info()    { echo -e "${BLUE}ℹ${NC}  $1"; }
success() { echo -e "${GREEN}✔${NC}  $1"; }
warn()    { echo -e "${YELLOW}⚠${NC}  $1"; }
error()   { echo -e "${RED}✘${NC}  $1"; }

# ═══════════════════════════════════════════════════════════════════════════════
#  UTILITIES
# ═══════════════════════════════════════════════════════════════════════════════

banner() {
  echo ""
  echo -e "${BOLD}${CYAN}┌──────────────────────────────────────────────┐${NC}"
  echo -e "${BOLD}${CYAN}│          ◆  Wixy Documentation  ◆           │${NC}"
  echo -e "${BOLD}${CYAN}│       Service Virtualization Platform        │${NC}"
  echo -e "${BOLD}${CYAN}└──────────────────────────────────────────────┘${NC}"
  echo ""
}

check_node() {
  if ! command -v node >/dev/null 2>&1; then
    error "Node.js is not installed."
    info "Install via: ${BOLD}brew install node${NC}  or  ${BOLD}https://nodejs.org${NC}"
    exit 1
  fi

  local node_major
  node_major="$(node -v | sed 's/v//' | cut -d. -f1)"
  if [[ "${node_major}" -lt 18 ]]; then
    error "Node.js >= 18 required. Found: $(node -v)"
    info "Update via: ${BOLD}brew upgrade node${NC}"
    exit 1
  fi
}

install_deps() {
  check_node

  if [[ ! -d "${DOCS_DIR}/node_modules" ]]; then
    info "Installing documentation dependencies..."
    (cd "${DOCS_DIR}" && npm install --silent)
    success "Dependencies installed."
  else
    success "Dependencies already installed."
  fi
}

is_port_available() {
  local port="${1}"
  if command -v lsof >/dev/null 2>&1; then
    if lsof -t -iTCP:"${port}" -sTCP:LISTEN >/dev/null 2>&1; then
      return 1
    fi
  fi
  return 0
}

kill_port_listeners() {
  local port="${1}"
  local pids=""

  if command -v lsof >/dev/null 2>&1; then
    pids="$(lsof -t -iTCP:"${port}" -sTCP:LISTEN 2>/dev/null || true)"
  fi

  if [[ -n "${pids}" ]]; then
    warn "Port ${port} in use — stopping process(es): ${pids}"
    echo "${pids}" | xargs kill -9 2>/dev/null || true
    sleep 1
  fi
}

pick_port() {
  local base_port="${1}"
  local try_port
  for try_port in "${base_port}" 3001 3002 3003 3004 3005; do
    if is_port_available "${try_port}"; then
      echo "${try_port}"
      return 0
    fi
  done
  return 1
}

is_running() {
  if [[ -f "${PID_FILE}" ]]; then
    local pid
    pid="$(cat "${PID_FILE}")"
    if kill -0 "${pid}" >/dev/null 2>&1; then
      return 0
    fi
  fi
  return 1
}

# ═══════════════════════════════════════════════════════════════════════════════
#  COMMANDS
# ═══════════════════════════════════════════════════════════════════════════════

cmd_serve() {
  banner

  if is_running; then
    warn "Documentation server is already running."
    cmd_status
    return 0
  fi

  install_deps

  local selected_port
  if ! selected_port="$(pick_port "${PORT}")"; then
    kill_port_listeners "${PORT}"
    selected_port="${PORT}"
  fi

  local addr="${HOST}:${selected_port}"
  echo "${addr}" > "${ADDR_FILE}"

  echo ""
  info "Starting Docusaurus dev server (hot-reload)..."

  (
    cd "${DOCS_DIR}"
    nohup npx docusaurus start --host "${HOST}" --port "${selected_port}" > "${LOG_FILE}" 2>&1 &
    echo $! > "${PID_FILE}"
  )

  # Wait for startup
  local retries=0
  while [[ ${retries} -lt 30 ]]; do
    if ! is_running; then
      break
    fi
    if ! is_port_available "${selected_port}"; then
      break
    fi
    sleep 1
    retries=$((retries + 1))
  done

  if is_running; then
    echo ""
    success "Documentation server running."
    echo ""
    echo -e "  ${BOLD}Open:${NC}    ${CYAN}http://${addr}${NC}"
    echo -e "  ${BOLD}Stop:${NC}    ${DIM}./docs.sh stop${NC}"
    echo -e "  ${BOLD}Status:${NC}  ${DIM}./docs.sh status${NC}"
    echo -e "  ${BOLD}Log:${NC}     ${DIM}${LOG_FILE}${NC}"
    echo ""
  else
    rm -f "${PID_FILE}" "${ADDR_FILE}"
    error "Server failed to start. Check log: ${LOG_FILE}"
    exit 1
  fi
}

cmd_build() {
  banner
  echo -e "  ${BOLD}${CYAN}▸ Building static site${NC}"
  echo ""

  install_deps

  info "Building Docusaurus site..."
  (cd "${DOCS_DIR}" && npx docusaurus build)

  echo ""
  success "Static site built → ${CYAN}${DOCS_DIR}/build/${NC}"
  echo ""
  echo -e "  ${DIM}To preview the production build:  ./docs.sh preview${NC}"
  echo ""
}

cmd_preview() {
  banner
  echo -e "  ${BOLD}${CYAN}▸ Production preview${NC}"
  echo ""

  if is_running; then
    warn "Stopping existing server first..."
    cmd_stop_quiet
  fi

  install_deps

  if [[ ! -d "${DOCS_DIR}/build" ]]; then
    warn "No build directory found. Building first..."
    (cd "${DOCS_DIR}" && npx docusaurus build)
  fi

  local selected_port
  if ! selected_port="$(pick_port "${PORT}")"; then
    kill_port_listeners "${PORT}"
    selected_port="${PORT}"
  fi

  local addr="${HOST}:${selected_port}"
  echo "${addr}" > "${ADDR_FILE}"

  info "Serving production build..."
  (
    cd "${DOCS_DIR}"
    nohup npx docusaurus serve --host "${HOST}" --port "${selected_port}" > "${LOG_FILE}" 2>&1 &
    echo $! > "${PID_FILE}"
  )

  sleep 3

  if is_running; then
    echo ""
    success "Production preview running."
    echo ""
    echo -e "  ${BOLD}Open:${NC}    ${CYAN}http://${addr}${NC}"
    echo -e "  ${BOLD}Stop:${NC}    ${DIM}./docs.sh stop${NC}"
    echo ""
  else
    rm -f "${PID_FILE}" "${ADDR_FILE}"
    error "Preview server failed to start. Check log: ${LOG_FILE}"
    exit 1
  fi
}

cmd_stop_quiet() {
  if [[ -f "${PID_FILE}" ]]; then
    local pid
    pid="$(cat "${PID_FILE}")"
    kill "${pid}" >/dev/null 2>&1 || true
    sleep 1
    # Force kill if still alive
    kill -9 "${pid}" >/dev/null 2>&1 || true
  fi

  if [[ -f "${ADDR_FILE}" ]]; then
    local addr port
    addr="$(cat "${ADDR_FILE}")"
    port="${addr##*:}"
    kill_port_listeners "${port}"
  else
    kill_port_listeners "${PORT}"
  fi

  rm -f "${PID_FILE}" "${ADDR_FILE}"
}

cmd_stop() {
  echo ""
  echo -e "  ${BOLD}${RED}▸ Stopping Documentation Server${NC}"
  echo ""

  if ! is_running && [[ ! -f "${PID_FILE}" ]]; then
    kill_port_listeners "${PORT}"
    info "No documentation server is running."
    echo ""
    return 0
  fi

  cmd_stop_quiet

  success "Documentation server stopped."
  echo ""
}

cmd_status() {
  echo ""
  if is_running; then
    local pid addr
    pid="$(cat "${PID_FILE}")"
    if [[ -f "${ADDR_FILE}" ]]; then
      addr="$(cat "${ADDR_FILE}")"
    else
      addr="${HOST}:${PORT}"
    fi
    success "Documentation server is ${BOLD}running${NC} ${DIM}(PID ${pid})${NC}"
    echo ""
    echo -e "  ${BOLD}URL:${NC}  ${CYAN}http://${addr}${NC}"
    echo -e "  ${BOLD}Log:${NC}  ${DIM}${LOG_FILE}${NC}"
  else
    info "Documentation server is ${BOLD}not running${NC}."
    echo ""
    echo -e "  ${DIM}Start with:  ./docs.sh serve${NC}"
  fi
  echo ""
}

cmd_clean() {
  echo ""
  echo -e "  ${BOLD}${YELLOW}▸ Cleaning Documentation Cache${NC}"
  echo ""

  if is_running; then
    warn "Stopping server before cleaning..."
    cmd_stop_quiet
  fi

  (cd "${DOCS_DIR}" && npx docusaurus clear 2>/dev/null || true)
  rm -rf "${DOCS_DIR}/build" "${DOCS_DIR}/.docusaurus"
  rm -f "${PID_FILE}" "${ADDR_FILE}" "${LOG_FILE}"

  success "Cache, build artifacts, and logs cleared."
  echo ""
}

cmd_help() {
  banner
  echo -e "${BOLD}Usage:${NC}  ./docs.sh [command]"
  echo ""
  echo -e "${BOLD}Commands:${NC}"
  echo -e "  ${BOLD}serve${NC}      Install deps + start dev server with hot-reload ${DIM}(default)${NC}"
  echo -e "  ${BOLD}build${NC}      Build static site to ./documentation/build"
  echo -e "  ${BOLD}preview${NC}    Build + serve production build locally"
  echo -e "  ${BOLD}stop${NC}       Stop the running documentation server"
  echo -e "  ${BOLD}status${NC}     Show whether the server is running"
  echo -e "  ${BOLD}clean${NC}      Clear Docusaurus cache and build artifacts"
  echo -e "  ${BOLD}help${NC}       Show this help message"
  echo ""
  echo -e "${BOLD}Environment:${NC}"
  echo -e "  ${CYAN}HOST${NC}       Bind address   ${DIM}(default: 127.0.0.1)${NC}"
  echo -e "  ${CYAN}PORT${NC}       Listen port    ${DIM}(default: 3000)${NC}"
  echo ""
  echo -e "${BOLD}Examples:${NC}"
  echo -e "  ./docs.sh                     ${DIM}# Start dev server (hot-reload)${NC}"
  echo -e "  ./docs.sh build               ${DIM}# Build for production (GitHub Pages)${NC}"
  echo -e "  ./docs.sh preview             ${DIM}# Preview production build locally${NC}"
  echo -e "  ./docs.sh stop                ${DIM}# Stop server${NC}"
  echo -e "  PORT=4000 ./docs.sh serve     ${DIM}# Start on custom port${NC}"
  echo ""
  echo -e "${BOLD}CI/CD:${NC}"
  echo -e "  The GitHub Actions workflow (${DIM}.github/workflows/docs.yml${NC}) builds and"
  echo -e "  deploys the documentation to GitHub Pages automatically on push to main."
  echo ""
}

# ═══════════════════════════════════════════════════════════════════════════════
#  MAIN — Command Router
# ═══════════════════════════════════════════════════════════════════════════════

command="${1:-}"
case "${command}" in
  serve)              cmd_serve ;;
  build)              cmd_build ;;
  preview)            cmd_preview ;;
  stop)               cmd_stop ;;
  status)             cmd_status ;;
  clean)              cmd_clean ;;
  help|--help|-h)     cmd_help ;;
  "")                 cmd_serve ;;
  *)
    error "Unknown command: ${command}"
    cmd_help
    exit 1
    ;;
esac
