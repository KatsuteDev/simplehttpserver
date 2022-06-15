/**
 * <h1>SimpleHttpServer Documentation</h1>
 *
 * <h2>Server</h2>
 * <ul>
 *     <li>{@link dev.katsute.simplehttpserver.SimpleHttpServer}</li>
 *     <li>{@link dev.katsute.simplehttpserver.SimpleHttpsServer}</li>
 *     <li>
 *         {@link dev.katsute.simplehttpserver.HttpSessionHandler}
 *         <ul>
 *             <li>{@link dev.katsute.simplehttpserver.HttpSession}</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <h2>Exchange</h2>
 * <ul>
 *     <li>{@link dev.katsute.simplehttpserver.SimpleHttpExchange}</li>
 *     <li>
 *         {@link dev.katsute.simplehttpserver.HttpSession}
 *         <ul>
 *             <li>{@link dev.katsute.simplehttpserver.HttpSessionHandler}</li>
 *         </ul>
 *     </li>
 *     <li>
 *         {@link dev.katsute.simplehttpserver.MultipartFormData}
 *         <ul>
 *             <li>{@link dev.katsute.simplehttpserver.Record}</li>
 *             <li>{@link dev.katsute.simplehttpserver.FileRecord}</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <h2>Handlers</h2>
 * <ul>
 *     <li>{@link dev.katsute.simplehttpserver.SimpleHttpHandler}</li>
 *     <li>{@link dev.katsute.simplehttpserver.handler.PredicateHandler}</li>
 *     <li>{@link dev.katsute.simplehttpserver.handler.RedirectHandler}</li>
 *     <li>{@link dev.katsute.simplehttpserver.handler.RootHandler}</li>
 *     <li>{@link dev.katsute.simplehttpserver.handler.SSEHandler}</li>
 *     <li>{@link dev.katsute.simplehttpserver.handler.TemporaryHandler}</li>
 *     <li>
 *         {@link dev.katsute.simplehttpserver.handler.file.FileHandler}
 *         <ul>
 *              <li>{@link dev.katsute.simplehttpserver.handler.file.FileAdapter}</li>
 *              <li>{@link dev.katsute.simplehttpserver.handler.file.FileOptions}</li>
 *         </ul>
 *     </li>
 *     <li>
 *         {@link dev.katsute.simplehttpserver.handler.throttler.ThrottledHandler}
 *         <ul>
 *             <li>{@link dev.katsute.simplehttpserver.handler.throttler.ExchangeThrottler}</li>
 *             <li>{@link dev.katsute.simplehttpserver.handler.throttler.ServerExchangeThrottler}</li>
 *             <li>{@link dev.katsute.simplehttpserver.handler.throttler.SessionThrottler}</li>
 *             <li>{@link dev.katsute.simplehttpserver.handler.throttler.ServerSessionThrottler}</li>
 *         </ul>
 *     </li>
 * </ul>
 */
module SimpleHttpServer {

    requires jdk.httpserver;

    exports dev.katsute.simplehttpserver.handler.file;
    exports dev.katsute.simplehttpserver.handler.throttler;
    exports dev.katsute.simplehttpserver.handler;
    exports dev.katsute.simplehttpserver;

}