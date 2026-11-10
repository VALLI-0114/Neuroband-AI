// PC-side check for gsr_cal.h. Run:  g++ test/check_gsr_cal.cpp -o check && ./check
#include <cassert>
#include <cmath>
#include <cstdio>
#include "../src/gsr_cal.h"

static bool near(float a, float b) { return std::fabs(a - b) < 1e-3f; }

int main() {
  const float us[] = {0.5f, 1.0f, 2.13f, 4.55f, 10.0f};
  const float up[] = {100, 300, 600, 1100, 2000};  // module where mV rises with conductance
  const float dn[] = {2000, 1100, 600, 300, 100};  // module where mV falls with conductance

  assert(gsrMvToUs(50, up, us, 5) == 0.0f);          // open electrodes
  assert(near(gsrMvToUs(300, up, us, 5), 1.0f));     // exact point
  assert(near(gsrMvToUs(450, up, us, 5), 1.565f));   // halfway 1.0 -> 2.13
  assert(near(gsrMvToUs(3000, up, us, 5), 10.0f));   // clamp high

  assert(gsrMvToUs(2500, dn, us, 5) == 0.0f);
  assert(near(gsrMvToUs(450, dn, us, 5), 3.34f));    // halfway 2.13 -> 4.55
  assert(near(gsrMvToUs(50, dn, us, 5), 10.0f));

  puts("gsr_cal ok");
}
