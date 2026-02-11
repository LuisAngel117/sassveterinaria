export default function Home() {
  return (
    <main className="min-h-screen bg-slate-50 px-6 py-12 text-slate-900">
      <section className="mx-auto max-w-3xl rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
        <p className="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500">
          SaaSVeterinaria
        </p>
        <h1 className="mt-4 text-3xl font-bold tracking-tight">
          Frontend inicial listo para SPR-F001
        </h1>
        <p className="mt-4 text-base text-slate-600">
          Este root usa Next.js con App Router y define comandos reales para
          trabajar el flujo de login, seleccion de sucursal y shell.
        </p>
        <ul className="mt-8 grid gap-3 text-sm text-slate-700 sm:grid-cols-2">
          <li className="rounded-xl border border-slate-200 bg-slate-50 p-4">
            <span className="font-semibold">Comando build</span>
            <p className="mt-1 font-mono text-xs">npm run build</p>
          </li>
          <li className="rounded-xl border border-slate-200 bg-slate-50 p-4">
            <span className="font-semibold">Comando dev</span>
            <p className="mt-1 font-mono text-xs">npm run dev</p>
          </li>
          <li className="rounded-xl border border-slate-200 bg-slate-50 p-4">
            <span className="font-semibold">API base</span>
            <p className="mt-1 font-mono text-xs">NEXT_PUBLIC_API_BASE_URL</p>
          </li>
          <li className="rounded-xl border border-slate-200 bg-slate-50 p-4">
            <span className="font-semibold">Proximo paso</span>
            <p className="mt-1 text-xs">Ejecutar sprint SPR-F001</p>
          </li>
        </ul>
      </section>
    </main>
  );
}
