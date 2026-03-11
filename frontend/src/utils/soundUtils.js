// Create and play different alarm sounds using Web Audio API
const audioContext = new (window.AudioContext || window.webkitAudioContext)()

const SOUND_CONFIGS = {
  'Bell': { frequency: 800, duration: 0.2, pattern: [0.2, 0.1, 0.2, 0.1, 0.2] },
  'Chime': { frequency: 1000, duration: 0.3, pattern: [0.3, 0.1, 0.2] },
  'Alert': { frequency: 600, duration: 0.4, pattern: [0.2, 0.1, 0.2, 0.1, 0.2, 0.1, 0.2] },
  'Melody': { frequencies: [600, 800, 1000], duration: 0.2, pattern: [0.2, 0.1, 0.2, 0.1, 0.2] },
  'Ping': { frequency: 1500, duration: 0.1, pattern: [0.1, 0.05, 0.1] },
}

export function playSound(tuneName) {
  const config = SOUND_CONFIGS[tuneName] || SOUND_CONFIGS['Bell']
  const now = audioContext.currentTime

  // Repeat pattern multiple times for more noticeable alert
  for (let repeat = 0; repeat < 3; repeat++) {
    let patternTime = now + repeat * 1.2

    config.pattern.forEach((duration) => {
      const osc = audioContext.createOscillator()
      const gain = audioContext.createGain()

      osc.connect(gain)
      gain.connect(audioContext.destination)

      osc.frequency.value = config.frequencies ? config.frequencies[0] : config.frequency
      osc.type = 'sine'

      gain.gain.setValueAtTime(0.3, patternTime)
      gain.gain.exponentialRampToValueAtTime(0.01, patternTime + duration)

      osc.start(patternTime)
      osc.stop(patternTime + duration)

      patternTime += duration
    })
  }
}

export function stopSound() {
  // Stop current audio context if needed
  if (audioContext.state === 'running') {
    // Audio context continues running, individual oscillators are stopped
  }
}
