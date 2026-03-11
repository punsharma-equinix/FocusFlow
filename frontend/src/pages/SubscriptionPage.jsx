import { useState } from 'react'
import { useAuthStore } from '../store'
import { userApi } from '../api'
import { Btn, Card, PageHeader, Divider } from '../components/ui'
import toast from 'react-hot-toast'

const PLANS = [
  {
    key: 'FREE', name: 'Free', price: '$0', period: '',
    color: 'var(--text2)',
    features: [
      '✓ Up to 10 tasks/day',
      '✓ Custom alarm tunes',
      '✓ Task priorities & categories',
      '✓ Basic completion stats',
      '✗ AI plan generation',
      '✗ AI chat refinement',
      '✗ AI weekly reports',
    ],
  },
  {
    key: 'PRO', name: 'Pro', price: '$9', period: '/mo',
    color: 'var(--accent)',
    highlight: true,
    features: [
      '✓ Unlimited tasks',
      '✓ All Free features',
      '✓ AI plan generation',
      '✓ AI chat & refinement',
      '✓ AI weekly insights',
      '✓ Auto-add tasks from AI',
      '✗ Priority AI responses',
    ],
  },
  {
    key: 'PREMIUM', name: 'Premium', price: '$19', period: '/mo',
    color: 'var(--gold)',
    features: [
      '✓ Everything in Pro',
      '✓ Priority AI responses',
      '✓ Export reports as PDF',
      '✓ Custom notification tunes',
      '✓ Social media integrations',
      '✓ Team / family sharing',
      '✓ Dedicated support',
    ],
  },
]

export default function SubscriptionPage() {
  const { user, setUser } = useAuthStore()
  const [loading, setLoading] = useState(null)

  async function upgrade(plan) {
    if (user?.plan === plan) return
    setLoading(plan)
    try {
      // In production: collect Stripe payment, pass token here
      const { data } = await userApi.upgradePlan(plan)
      setUser(data)
      toast.success(`Upgraded to ${plan} plan!`)
    } catch {
      toast.error('Could not upgrade plan. Try again.')
    }
    setLoading(null)
  }

  return (
    <div className="fade-in">
      <PageHeader title="Plans & Billing" sub="Choose the plan that fits your ambition" />

      <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap', marginBottom: 32 }}>
        {PLANS.map((plan) => {
          const isCurrent = user?.plan === plan.key
          return (
            <div key={plan.key} style={{
              flex: '1 1 220px',
              background: 'var(--card)',
              border: `1px solid ${plan.highlight ? 'var(--accent)' : isCurrent ? plan.color : 'var(--border)'}`,
              borderRadius: 'var(--radius-lg)', padding: '24px 22px',
              position: 'relative',
              boxShadow: plan.highlight ? '0 0 30px var(--accent-s2)' : 'none',
            }}>
              {plan.highlight && (
                <div style={{
                  position: 'absolute', top: -12, left: '50%', transform: 'translateX(-50%)',
                  background: 'var(--accent)', color: '#fff',
                  fontSize: 10, fontWeight: 700, padding: '4px 14px', borderRadius: 20,
                }}>
                  MOST POPULAR
                </div>
              )}

              <div style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: 18, color: 'var(--white)' }}>
                {plan.name}
              </div>
              <div style={{ marginTop: 8, marginBottom: 18 }}>
                <span style={{ fontSize: 32, fontWeight: 800, color: plan.color, fontFamily: 'var(--font-display)' }}>{plan.price}</span>
                <span style={{ fontSize: 14, color: 'var(--text2)' }}>{plan.period}</span>
              </div>

              <Divider style={{ margin: '0 0 16px' }} />

              {plan.features.map((f) => (
                <div key={f} style={{ fontSize: 13, color: f.startsWith('✗') ? 'var(--text2)' : 'var(--text)', marginBottom: 8, lineHeight: 1.5 }}>
                  {f}
                </div>
              ))}

              <div style={{ marginTop: 22 }}>
                <Btn
                  full
                  variant={isCurrent ? 'ghost' : 'primary'}
                  loading={loading === plan.key}
                  onClick={() => upgrade(plan.key)}
                  disabled={isCurrent}
                  style={{ background: isCurrent ? undefined : plan.color === 'var(--accent)' ? undefined : plan.color }}
                >
                  {isCurrent ? '✓ Current Plan' : `Get ${plan.name}`}
                </Btn>
              </div>
            </div>
          )
        })}
      </div>

      {/* Billing info */}
      <Card>
        <div style={{ fontFamily: 'var(--font-display)', fontWeight: 700, color: 'var(--white)', marginBottom: 12 }}>Billing Info</div>
        <div style={{ fontSize: 13, color: 'var(--text2)', lineHeight: 1.8 }}>
          Current plan: <strong style={{ color: 'var(--accent)' }}>{user?.plan}</strong>
          &nbsp;· Member since: {user?.createdAt?.slice(0, 10)}
        </div>
        <Divider />
        <div style={{ fontSize: 12, color: 'var(--text2)' }}>
          💳 Payments are processed securely via Stripe. Cancel anytime from your billing portal.
          API keys and payment tokens are never stored on our servers.
        </div>
      </Card>
    </div>
  )
}
