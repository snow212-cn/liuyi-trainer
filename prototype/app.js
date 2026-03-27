const previewCadence = {
  id: "preview_default",
  label: "2-1-2 原型节奏",
  eccentricSeconds: 2,
  bottomPauseSeconds: 1,
  concentricSeconds: 2,
  source: "当前按用户确认的 2-1-2 节奏原型演示"
};

const families = [
  { id: "pushup", zh: "俯卧撑", en: "Pushup", summary: "上肢推力链训练" },
  { id: "squat", zh: "深蹲", en: "Squat", summary: "下肢屈伸链训练" },
  { id: "pullup", zh: "引体", en: "Pullup", summary: "上肢拉力链训练" },
  { id: "leg_raise", zh: "举腿", en: "Leg Raise", summary: "躯干前链训练" },
  { id: "bridge", zh: "桥", en: "Bridge", summary: "躯干后链训练" },
  { id: "handstand_pushup", zh: "倒立撑", en: "Handstand Pushup", summary: "垂直推力链训练" }
];

const phases = [
  { key: "lowering", label: "下落", durationMs: previewCadence.eccentricSeconds * 1000, cue: "落" },
  { key: "bottom_hold", label: "停", durationMs: previewCadence.bottomPauseSeconds * 1000, cue: "" },
  { key: "rising", label: "起", durationMs: previewCadence.concentricSeconds * 1000, cue: "起" }
];

const cycleMs = phases.reduce((sum, phase) => sum + phase.durationMs, 0);

const state = {
  mode: "idle",
  voiceEnabled: false,
  sessionStartedAt: null,
  sessionEndedAt: null,
  setStartedAt: null,
  restStartedAt: null,
  timer: null,
  lastPhaseKey: null,
  restPresetSeconds: 90,
  sets: []
};

const elements = {
  familySelect: document.getElementById("familySelect"),
  stepSelect: document.getElementById("stepSelect"),
  restPresetSelect: document.getElementById("restPresetSelect"),
  totalSets: document.getElementById("totalSets"),
  totalReps: document.getElementById("totalReps"),
  sessionStateBox: document.getElementById("sessionStateBox"),
  trainingBoard: document.getElementById("trainingBoard"),
  restBoard: document.getElementById("restBoard"),
  currentExerciseText: document.getElementById("currentExerciseText"),
  nextSetText: document.getElementById("nextSetText"),
  phaseBox: document.getElementById("phaseBox"),
  phaseSecondBox: document.getElementById("phaseSecondBox"),
  repBox: document.getElementById("repBox"),
  restTitleBox: document.getElementById("restTitleBox"),
  restCaptionBox: document.getElementById("restCaptionBox"),
  restClockBox: document.getElementById("restClockBox"),
  setIndexBox: document.getElementById("setIndexBox"),
  elapsedBox: document.getElementById("elapsedBox"),
  completedSetsBox: document.getElementById("completedSetsBox"),
  restPresetBox: document.getElementById("restPresetBox"),
  statusText: document.getElementById("statusText"),
  sessionText: document.getElementById("sessionText"),
  setTableBody: document.getElementById("setTableBody"),
  familyList: document.getElementById("familyList"),
  startBtn: document.getElementById("startBtn"),
  endBtn: document.getElementById("endBtn"),
  nextSetBtn: document.getElementById("nextSetBtn"),
  voiceBtn: document.getElementById("voiceBtn"),
  finishBtn: document.getElementById("finishBtn"),
  resetBtn: document.getElementById("resetBtn")
};

function init() {
  families.forEach((family) => {
    const option = document.createElement("option");
    option.value = family.id;
    option.textContent = `${family.zh} / ${family.en}`;
    elements.familySelect.appendChild(option);
  });

  for (let index = 1; index <= 10; index += 1) {
    const option = document.createElement("option");
    option.value = String(index);
    option.textContent = `第 ${index} 式`;
    elements.stepSelect.appendChild(option);
  }

  families.forEach((family) => {
    const item = document.createElement("div");
    item.className = "family-item";
    item.innerHTML = `
      <div class="family-top">
        <strong>${family.zh}</strong>
        <span class="pill">10 式</span>
      </div>
      <div class="small">${family.en}</div>
      <div class="steps">${family.summary}。正式标准内容后续接入。</div>
    `;
    elements.familyList.appendChild(item);
  });

  elements.restPresetSelect.addEventListener("change", updateRestPreset);
  elements.startBtn.addEventListener("click", startSet);
  elements.endBtn.addEventListener("click", endSet);
  elements.nextSetBtn.addEventListener("click", startNextSet);
  elements.voiceBtn.addEventListener("click", toggleVoice);
  elements.finishBtn.addEventListener("click", finishSession);
  elements.resetBtn.addEventListener("click", resetSession);

  updateRestPreset();
  render();
}

function selectedFamily() {
  return families.find((item) => item.id === elements.familySelect.value) || families[0];
}

function selectedStep() {
  return Number(elements.stepSelect.value || "1");
}

function currentSetIndex() {
  if (state.mode === "rest" || state.mode === "rest_overtime") {
    return state.sets.length + 1;
  }
  return state.sets.length + 1;
}

function formatElapsed(ms) {
  const safeMs = Math.max(0, ms);
  const totalSeconds = safeMs / 1000;
  const minutes = String(Math.floor(totalSeconds / 60)).padStart(2, "0");
  const seconds = String(Math.floor(totalSeconds % 60)).padStart(2, "0");
  const tenth = Math.floor((safeMs % 1000) / 100);
  return `${minutes}:${seconds}.${tenth}`;
}

function formatClock(ms) {
  const safeMs = Math.max(0, ms);
  const totalSeconds = Math.floor(safeMs / 1000);
  const minutes = String(Math.floor(totalSeconds / 60)).padStart(2, "0");
  const seconds = String(totalSeconds % 60).padStart(2, "0");
  return `${minutes}:${seconds}`;
}

function formatDateTime(timestamp) {
  if (!timestamp) {
    return "未开始";
  }
  return new Date(timestamp).toLocaleString("zh-CN", { hour12: false });
}

function getProgress(elapsedMs) {
  const safeElapsedMs = Math.max(0, elapsedMs);
  const reps = Math.floor(safeElapsedMs / cycleMs);
  const cyclePosition = safeElapsedMs % cycleMs;
  let offset = 0;

  for (const phase of phases) {
    if (cyclePosition < offset + phase.durationMs) {
      return {
        reps,
        phaseKey: phase.key,
        phaseLabel: phase.label,
        phaseElapsedMs: cyclePosition - offset,
        cue: phase.cue
      };
    }
    offset += phase.durationMs;
  }

  return {
    reps,
    phaseKey: phases[phases.length - 1].key,
    phaseLabel: phases[phases.length - 1].label,
    phaseElapsedMs: 0,
    cue: phases[phases.length - 1].cue
  };
}

function speak(text) {
  if (!state.voiceEnabled || !text || !("speechSynthesis" in window)) {
    return;
  }
  window.speechSynthesis.cancel();
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = "zh-CN";
  utterance.rate = 1;
  window.speechSynthesis.speak(utterance);
}

function startTicker() {
  if (state.timer) {
    return;
  }
  state.timer = window.setInterval(tick, 100);
}

function stopTicker() {
  if (!state.timer) {
    return;
  }
  window.clearInterval(state.timer);
  state.timer = null;
}

function startSet() {
  if (state.mode === "set") {
    return;
  }

  if (!state.sessionStartedAt) {
    state.sessionStartedAt = Date.now();
  }

  state.mode = "set";
  state.sessionEndedAt = null;
  state.setStartedAt = Date.now();
  state.restStartedAt = null;
  state.lastPhaseKey = null;

  elements.statusText.textContent = `已开始：${selectedFamily().zh} 第 ${selectedStep()} 式。当前按 2-1-2 节奏实时引导。`;
  startTicker();
  render();
}

function endSet() {
  if (state.mode !== "set") {
    return;
  }

  const endedAt = Date.now();
  const elapsedMs = endedAt - state.setStartedAt;
  const progress = getProgress(elapsedMs);

  state.sets.push({
    index: state.sets.length + 1,
    familyZh: selectedFamily().zh,
    step: selectedStep(),
    startedAt: state.setStartedAt,
    endedAt,
    elapsedMs,
    reps: progress.reps
  });

  state.mode = "rest";
  state.setStartedAt = null;
  state.restStartedAt = endedAt;
  state.lastPhaseKey = null;

  elements.statusText.textContent = `已完成第 ${state.sets.length} 组，记录 ${progress.reps} 次。现在进入组间休息。`;
  render();
}

function startNextSet() {
  if (state.mode !== "rest" && state.mode !== "rest_overtime") {
    return;
  }
  startSet();
}

function finishSession() {
  if (state.mode === "set") {
    elements.statusText.textContent = "请先结束当前这一组，再结束本次训练。";
    return;
  }

  if (state.sessionStartedAt && !state.sessionEndedAt) {
    state.sessionEndedAt = Date.now();
  }

  state.mode = "completed";
  stopTicker();

  const totalReps = state.sets.reduce((sum, item) => sum + item.reps, 0);
  elements.statusText.textContent = state.sets.length
    ? `本次训练结束。共 ${state.sets.length} 组，总次数 ${totalReps} 次。`
    : "当前还没有任何训练记录。";
  render();
}

function resetSession() {
  stopTicker();

  state.mode = "idle";
  state.voiceEnabled = false;
  state.sessionStartedAt = null;
  state.sessionEndedAt = null;
  state.setStartedAt = null;
  state.restStartedAt = null;
  state.lastPhaseKey = null;
  state.restPresetSeconds = Number(elements.restPresetSelect.value || "90");
  state.sets = [];

  elements.statusText.textContent = "当前未开始。请选择动作与休息时长后，点击“开始本组”。";
  render();
}

function updateRestPreset() {
  state.restPresetSeconds = Number(elements.restPresetSelect.value || "90");
  elements.restPresetBox.textContent = `${state.restPresetSeconds} 秒`;
  render();
}

function toggleVoice() {
  state.voiceEnabled = !state.voiceEnabled;
  elements.statusText.textContent = state.voiceEnabled
    ? "语音提示已开启。浏览器支持时，会在阶段切换时播报“起”“落”。"
    : "语音提示已关闭。";
  render();
}

function tick() {
  if (state.mode === "set") {
    const elapsedMs = Date.now() - state.setStartedAt;
    const progress = getProgress(elapsedMs);

    if (progress.phaseKey !== state.lastPhaseKey) {
      state.lastPhaseKey = progress.phaseKey;
      speak(progress.cue);
    }
  } else if (state.mode === "rest" || state.mode === "rest_overtime") {
    const restElapsedMs = Date.now() - state.restStartedAt;
    const remainingMs = state.restPresetSeconds * 1000 - restElapsedMs;
    state.mode = remainingMs >= 0 ? "rest" : "rest_overtime";
  }

  render();
}

function renderTraining(progress, elapsedMs) {
  elements.trainingBoard.hidden = false;
  elements.restBoard.hidden = true;
  elements.phaseBox.textContent = progress.phaseLabel;
  elements.phaseSecondBox.textContent = `${(progress.phaseElapsedMs / 1000).toFixed(1)} 秒`;
  elements.repBox.textContent = String(progress.reps);
  elements.elapsedBox.textContent = formatElapsed(elapsedMs);
  elements.sessionStateBox.textContent = "训练中";
}

function renderRest(restElapsedMs, remainingMs) {
  elements.trainingBoard.hidden = true;
  elements.restBoard.hidden = false;
  elements.restTitleBox.textContent = state.mode === "rest" ? "组间休息" : "休息超时";
  elements.restCaptionBox.textContent = state.mode === "rest" ? "建议休息剩余" : "已超出建议休息";
  elements.restClockBox.textContent = formatClock(Math.abs(remainingMs));
  elements.elapsedBox.textContent = formatElapsed(restElapsedMs);
  elements.sessionStateBox.textContent = state.mode === "rest" ? "休息中" : "休息超时";
}

function renderStaticFocus() {
  elements.trainingBoard.hidden = false;
  elements.restBoard.hidden = true;
  elements.phaseBox.textContent = state.mode === "completed" ? "已完成" : "未开始";
  elements.phaseSecondBox.textContent = "0.0 秒";
  elements.repBox.textContent = "0";
  elements.elapsedBox.textContent = "00:00.0";
  elements.sessionStateBox.textContent = state.mode === "completed" ? "本次训练已结束" : "未开始";
}

function renderButtons() {
  elements.startBtn.disabled = state.mode !== "idle";
  elements.endBtn.disabled = state.mode !== "set";
  elements.nextSetBtn.disabled = !(state.mode === "rest" || state.mode === "rest_overtime");
  elements.voiceBtn.textContent = `语音提示：${state.voiceEnabled ? "开" : "关"}`;
}

function renderSessionMeta() {
  const completedSetCount = state.sets.length;
  const totalReps = state.sets.reduce((sum, item) => sum + item.reps, 0);

  elements.totalSets.textContent = String(completedSetCount);
  elements.totalReps.textContent = String(totalReps);
  elements.currentExerciseText.textContent = `${selectedFamily().zh} 第 ${selectedStep()} 式`;
  elements.nextSetText.textContent = `下一组：第 ${completedSetCount + 1} 组`;
  elements.setIndexBox.textContent = `第 ${currentSetIndex()} 组`;
  elements.completedSetsBox.textContent = `${completedSetCount} 组`;
  elements.sessionText.innerHTML = `本次训练开始时间：${formatDateTime(state.sessionStartedAt)}<br />本次训练结束时间：${state.sessionEndedAt ? formatDateTime(state.sessionEndedAt) : "未结束"}`;
}

function renderSetTable() {
  elements.setTableBody.innerHTML = "";
  state.sets.forEach((item) => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${item.index}</td>
      <td>${item.familyZh} 第 ${item.step} 式</td>
      <td>${formatDateTime(item.startedAt)}</td>
      <td>${formatDateTime(item.endedAt)}</td>
      <td>${formatElapsed(item.elapsedMs)}</td>
      <td>${item.reps} 次</td>
    `;
    elements.setTableBody.appendChild(row);
  });
}

function render() {
  if (state.mode === "set" && state.setStartedAt) {
    const elapsedMs = Date.now() - state.setStartedAt;
    renderTraining(getProgress(elapsedMs), elapsedMs);
  } else if ((state.mode === "rest" || state.mode === "rest_overtime") && state.restStartedAt) {
    const restElapsedMs = Date.now() - state.restStartedAt;
    const remainingMs = state.restPresetSeconds * 1000 - restElapsedMs;
    renderRest(restElapsedMs, remainingMs);
  } else {
    renderStaticFocus();
  }

  renderButtons();
  renderSessionMeta();
  renderSetTable();
}

init();
