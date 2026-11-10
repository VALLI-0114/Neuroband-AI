#pragma once
// Piecewise-linear map: electrode voltage (mV) -> skin conductance (uS).
// calUs is ordered low -> high conductance. calMv may rise or fall with conductance
// (depends on the GSR module), both work. Less conductive than the first point
// (open electrodes, no skin) returns 0. Beyond the last point clamps to the last value.
inline float gsrMvToUs(float mv, const float* calMv, const float* calUs, int n) {
  bool rising = calMv[n - 1] > calMv[0];
  if (rising ? mv < calMv[0] : mv > calMv[0]) return 0.0f;
  for (int i = 0; i + 1 < n; i++) {
    float a = calMv[i], b = calMv[i + 1];
    if ((mv - a) * (mv - b) <= 0) {  // mv lies between a and b
      if (a == b) return calUs[i];
      return calUs[i] + (calUs[i + 1] - calUs[i]) * (mv - a) / (b - a);
    }
  }
  return calUs[n - 1];
}
